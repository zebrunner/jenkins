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
def zbrPipelineURL = env['ZEBRUNNER_PIPELINE']
def zbrPipelineVersion = env['ZEBRUNNER_VERSION']

def zbrLogLevel = env['ZEBRUNNER_LOG_LEVEL']

def sonarUrl = env['SONAR_URL']

// Constants
def instance = Jenkins.getInstance()
def global_domain = Domain.global()

def credentialsStore = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

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

if(!envVars.containsKey("JENKINS_SECURITY_INITIALIZED") || "false".equalsIgnoreCase(envVars.get("JENKINS_SECURITY_INITIALIZED"))) {
  println "Apply Zebrunner Pipeline 1.0 settings"

  //https://github.com/qaprosoft/jenkins-master/issues/12 - remove default 5 sec quite period for Jenkins
  instance.setQuietPeriod(0)
  instance.setNumExecutors(10)

  jlc = JenkinsLocationConfiguration.get()
  jlc.setUrl(rootURL)
  jlc.setAdminAddress(rootEmail)
  jlc.save()

  println "INFRA_HOST: " + infraHost
  envVars.put("INFRA_HOST", infraHost)

  println "ADMIN_EMAILS: " + adminEmails
  envVars.put("ADMIN_EMAILS", adminEmails)

  println "SONAR_URL: " + sonarUrl
  envVars.put("SONAR_URL", sonarUrl)

  // #388 declare new SONAR_TOKEN global env var
  envVars.put("SONAR_TOKEN", "")

  // #166: NPE during disabling CLI: java.lang.NullPointerException: Cannot invoke method get() on null object
  // Commented below obsolete codeline
  //instance.getDescriptor("jenkins.CLI").get().setEnabled(false)

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

  // IMPORTANT! don't append any functionality below as settings security restrict a lot of access. Put them above "setting security" step to have full admin privileges

  //set global var to true to define that initial setup is finished
  envVars.put("JENKINS_SECURITY_INITIALIZED", "true")

} else {
  println "Zebrunner Jenkins global initialization already happened."
}

if(!envVars.containsKey("ZEBRUNNER_VERSION")) {
  println "Apply Zebrunner Pipeline 1.1 settings"

  println "Put Zebrunner Pipeline log level: " + zbrLogLevel
  envVars.put("ZEBRUNNER_LOG_LEVEL", zbrLogLevel)
}

// TODO: in case of major upgrade with changes in general settings - put steps HERE based on ZEBRUNNER_VERSION property


// Forcibly migrate to the latest version of Zebrunner Pipeline during restart
println "Put Zebrunner Pipeline URL: " + zbrPipelineURL
envVars.put("ZEBRUNNER_PIPELINE", zbrPipelineURL)

println "Put Zebrunner Pipeline version: " + zbrPipelineVersion
envVars.put("ZEBRUNNER_VERSION", zbrPipelineVersion)

// Save the state
instance.save()
