import hudson.model.*;
import jenkins.model.*;
import hudson.security.*;
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.impl.*;
import com.cloudbees.plugins.credentials.common.*;
import com.cloudbees.plugins.credentials.domains.*;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*;
import org.jenkinsci.plugins.workflow.flow.GlobalDefaultFlowDurabilityLevel;
import org.jenkinsci.plugins.workflow.flow.FlowDurabilityHint;
import java.lang.reflect.Field;
import org.jenkinsci.plugins.ghprb.GhprbGitHubAuth;
import hudson.util.Secret;

// Disable Jenkins security that blocks eTAF reports
System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "default-src 'self'; script-src 'self' https://ajax.googleapis.com 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self'")
System.setProperty("hudson.model.ParametersAction.keepUndefinedParameters", "true")

// Variables
def env = System.getenv()

def rootURL = env['ROOT_URL']
def rootEmail = env['ROOT_EMAIL']

def adminEmails = env['ADMIN_EMAILS']

def user = env['ADMIN_USER']
def pass = env['ADMIN_PASS']

def infraHost = env['INFRA_HOST']
def qpsPipelineGitURL = env['QPS_PIPELINE_GIT_URL']
def qpsPipelineGitBranch = env['QPS_PIPELINE_GIT_BRANCH']

def qpsPipelineLogLevel = env['QPS_PIPELINE_LOG_LEVEL']

def sonarUrl = env['SONAR_URL']

// Constants
def instance = Jenkins.getInstance()
def global_domain = Domain.global()

def credentialsStore = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

def id = "ghprbhook-token"
def username = env['GHPRBHOOK_USER']
def password = env['GHPRBHOOK_PASS']
def description = "GitHub Pull Request Builder token"

def ghprbhookCredentials = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    id,
    description,
    username,
    password
)

//https://github.com/qaprosoft/jenkins-master/issues/12 - remove default 5 sec quite period for Jenkins
instance.setQuietPeriod(0)
instance.setNumExecutors(10)

Thread.start {
    println "--> Configuring General Settings"

    // Global Environment Variables
    // Source: https://groups.google.com/forum/#!topic/jenkinsci-users/KgCGuDmED1Q
    globalNodeProperties = instance.getGlobalNodeProperties()
    envVarsNodePropertyList = globalNodeProperties.getAll(hudson.slaves.EnvironmentVariablesNodeProperty.class)

    newEnvVarsNodeProperty = null
    envVars = null

    if ( envVarsNodePropertyList == null || envVarsNodePropertyList.size() == 0 ) {
      newEnvVarsNodeProperty = new hudson.slaves.EnvironmentVariablesNodeProperty();
      globalNodeProperties.add(newEnvVarsNodeProperty)
      envVars = newEnvVarsNodeProperty.getEnvVars()
    } else {
      envVars = envVarsNodePropertyList.get(0).getEnvVars()
    }

    // Base URL
    println "--> Setting Base URL"
    if(!envVars.containsKey("JENKINS_SECURITY_INITIALIZED") || envVars.get("JENKINS_SECURITY_INITIALIZED") != "true") {
        jlc = JenkinsLocationConfiguration.get()
        jlc.setUrl(rootURL)
        jlc.setAdminAddress(rootEmail)
        jlc.save()
    }

    if ( infraHost != null && !envVars.containsKey("INFRA_HOST") ) {
      envVars.put("INFRA_HOST", infraHost)
    }

    if ( qpsPipelineGitURL != null && !envVars.containsKey("QPS_PIPELINE_GIT_URL") ) {
      envVars.put("QPS_PIPELINE_GIT_URL", qpsPipelineGitURL)
    }

    if ( qpsPipelineGitBranch != null && !envVars.containsKey("QPS_PIPELINE_GIT_BRANCH") ) {
      envVars.put("QPS_PIPELINE_GIT_BRANCH", qpsPipelineGitBranch)
    }

    if ( qpsPipelineLogLevel != null && !envVars.containsKey("QPS_PIPELINE_LOG_LEVEL") ) {
      envVars.put("QPS_PIPELINE_LOG_LEVEL", qpsPipelineLogLevel)
    }

    if ( adminEmails != null && !envVars.containsKey("ADMIN_EMAILS") ) {
      envVars.put("ADMIN_EMAILS", adminEmails)
    }

    if (sonarUrl != null && !envVars.containsKey("SONAR_URL")) {
        envVars.put("SONAR_URL", sonarUrl)
    }

    // #166: NPE during disabling CLI: java.lang.NullPointerException: Cannot invoke method get() on null object
    // Commented below obsolete codeline
    //instance.getDescriptor("jenkins.CLI").get().setEnabled(false)

    if(!envVars.containsKey("JENKINS_SECURITY_INITIALIZED") || envVars.get("JENKINS_SECURITY_INITIALIZED") != "true") {
        println "--> setting ghprhook creds"

        credentialsStore.addCredentials(global_domain, ghprbhookCredentials)
        def descriptor = Jenkins.instance.getDescriptorByType(org.jenkinsci.plugins.ghprb.GhprbTrigger.DescriptorImpl.class)
        Field auth = descriptor.class.getDeclaredField("githubAuth")
        auth.setAccessible(true)
        def githubAuth = new ArrayList<GhprbGitHubAuth>(1)

        Secret secret = Secret.fromString('')
        githubAuth.add(new GhprbGitHubAuth("https://api.github.com", "", id, description, username, secret))
        auth.set(descriptor, githubAuth)

        descriptor.save()

        println "--> setting security"

        def hudsonRealm = new HudsonPrivateSecurityRealm(false)
        hudsonRealm.createAccount(user, pass)
        instance.setSecurityRealm(hudsonRealm)

        def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
        strategy.setAllowAnonymousRead(false)
        instance.setAuthorizationStrategy(strategy)
        instance.save()

      println "--> setting pipeline speed/durability settings"
      
      GlobalDefaultFlowDurabilityLevel.DescriptorImpl level = instance.getExtensionList(GlobalDefaultFlowDurabilityLevel.DescriptorImpl.class).get(0);
      level.setDurabilityHint(FlowDurabilityHint.PERFORMANCE_OPTIMIZED);
    }

    // IMPORTANT! don't append any functionality below as settings security restrict a lot of access. Put them above "setting security" step to have full admin privileges

    //set global var to true to define that initial setup is finished
    envVars.put("JENKINS_SECURITY_INITIALIZED", "true")

    // Save the state
    instance.save()
}
