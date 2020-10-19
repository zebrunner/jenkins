import hudson.model.*;
import jenkins.model.*;
import hudson.security.*
import com.cloudbees.plugins.credentials.impl.*;
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.domains.*;

// Disable Jenkins security that blocks eTAF reports
System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "default-src 'self'; script-src 'self' https://ajax.googleapis.com 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self'")
System.setProperty("hudson.model.ParametersAction.keepUndefinedParameters", "true")

// Variables
def env = System.getenv()

//TODO: update with actual value for your CI for example: 
//def rootURL = "http://mydomain.com/jenkins"
def rootURL = null

// comma sepratated string of qps-infra administrators. Persons who will receive system failures notification about qps-infra jobdsl/pipeline.
//TODO: update with actual values for your CI
def adminEmails = "admin@mydomain.com,admin2@mydonain.com"

//TODO: update with actual fully qualified domain name or ip address
def infraHost = "CHANGE_ME"

def zbrPipelineURL = "https://github.com/zebrunner/pipeline-ce.git"
def zbrPipelineVersion = "1.1"

def zbrLogLevel = "INFO"

// Constants
def instance = Jenkins.getInstance()


println "--> Configuring General Settings"

println "--> Global Environment Variables"

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

if(!envVars.containsKey("JENKINS_SECURITY_INITIALIZED") || envVars.get("JENKINS_SECURITY_INITIALIZED") != "true") {
  jlc = JenkinsLocationConfiguration.get()
  if (rootURL != null) {
    // override root url when not null
    jlc.setUrl(rootURL)
  }
  jlc.setAdminAddress(rootEmail)
  jlc.save()
}

if ( infraHost != null && !envVars.containsKey("INFRA_HOST") ) {
  envVars.put("INFRA_HOST", infraHost)
}

if ( zbrPipelineURL != null && !envVars.containsKey("ZEBRUNNER_PIPELINE") ) {
  envVars.put("ZEBRUNNER_PIPELINE", zbrPipelineURL)
}

if ( zbrPipelineVersion != null && !envVars.containsKey("ZEBRUNNER_VERSION") ) {
  envVars.put("ZEBRUNNER_VERSION", zbrPipelineVersion)
}

if ( zbrLogLevel != null && !envVars.containsKey("ZEBRUNNER_LOG_LEVEL") ) {
  envVars.put("ZEBRUNNER_LOG_LEVEL", zbrLogLevel)
}

if ( adminEmails != null && !envVars.containsKey("ADMIN_EMAILS") ) {
  envVars.put("ADMIN_EMAILS", adminEmails)
}


// Setup security
if(!envVars.containsKey("JENKINS_SECURITY_INITIALIZED") || envVars.get("JENKINS_SECURITY_INITIALIZED") != "true")
{
    envVars.put("JENKINS_SECURITY_INITIALIZED", "true")
}

// Save the state
instance.save()

