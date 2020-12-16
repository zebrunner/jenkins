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

//TODO: update with actual fully qualified domain name or ip address
def infraHost = "CHANGE_ME"

def zbrPipelineURL = "https://github.com/zebrunner/pipeline-ce.git"
def zbrPipelineVersion = "1.3"
def zbrLogLevel = "INFO"

// Constants
def instance = Jenkins.getInstance()

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

envVars.put("INFRA_HOST", infraHost)
envVars.put("ZEBRUNNER_PIPELINE", zbrPipelineURL)
envVars.put("ZEBRUNNER_VERSION", zbrPipelineVersion)
envVars.put("ZEBRUNNER_LOG_LEVEL", zbrLogLevel)
envVars.put("JENKINS_SECURITY_INITIALIZED", "true")

// Save the state
instance.save()

