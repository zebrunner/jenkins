import hudson.model.*;
import jenkins.model.*;
import hudson.security.*
import com.cloudbees.plugins.credentials.impl.*;
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.domains.*;

// Disable Jenkins security that blocks eTAF reports
System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "default-src 'self'; script-src 'self' https://ajax.googleapis.com 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self'")

// Variables
def env = System.getenv()

def rootURL = env['ROOT_URL']
def rootEmail = env['ROOT_EMAIL']

def adminEmails = env['ADMIN_EMAILS']

def user = env['ADMIN_USER']
def pass = env['ADMIN_PASS']

def qpsHost = env['QPS_HOST']
def zafiraAccessToken = env['ZAFIRA_ACCESS_TOKEN']
def qpsPipelineGitURL = env['QPS_PIPELINE_GIT_URL']
def qpsPipelineGitBranch = env['QPS_PIPELINE_GIT_BRANCH']

def qpsPipelineLogLevel = env['QPS_PIPELINE_LOG_LEVEL']

// def globalPipelineLib = env['GLOBAL_PIPELINE_LIB']


// Constants
def instance = Jenkins.getInstance()

//https://github.com/qaprosoft/jenkins-master/issues/12 - remove default 5 sec quite period for Jenkins
instance.setQuietPeriod(0)
instance.setNumExecutors(10)

Thread.start {
    println "--> Configuring General Settings"

    // Base URL
    println "--> Setting Base URL"
    jlc = JenkinsLocationConfiguration.get()
    jlc.setUrl(rootURL)
    jlc.setAdminAddress(rootEmail)
    jlc.save()

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


    if ( qpsHost != null && !envVars.containsKey("QPS_HOST") ) {
      envVars.put("QPS_HOST", qpsHost)
    }

    if ( zafiraAccessToken != null && !envVars.containsKey("ZAFIRA_ACCESS_TOKEN") ) {
      envVars.put("ZAFIRA_ACCESS_TOKEN", zafiraAccessToken)
    }

//    if ( globalPipelineLib != null && !envVars.containsKey("GLOBAL_PIPELINE_LIB") ) {
//      envVars.put("GLOBAL_PIPELINE_LIB", globalPipelineLib)
//    }

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


    // Setup security
    if(!envVars.containsKey("JENKINS_SECURITY_INITIALIZED") || envVars.get("JENKINS_SECURITY_INITIALIZED") != "true")
    {
        def hudsonRealm = new HudsonPrivateSecurityRealm(false)
        hudsonRealm.createAccount(user, pass)
        instance.setSecurityRealm(hudsonRealm)

        def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
        instance.setAuthorizationStrategy(strategy)

        envVars.put("JENKINS_SECURITY_INITIALIZED", "true")
    }

    instance.getDescriptor("jenkins.CLI").get().setEnabled(false)
    // Save the state
    instance.save()
}
