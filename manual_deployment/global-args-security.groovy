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

// comma sepratated string of qps-infra administrators. Persons who will receive system failures notification about qps-infra jobdsl/pipeline.
//TODO: update with actual values for your CI
def adminEmails = "admin@mydomain.com,admin2@mydonain.com"

//TODO: update with actual fully qualified domain name or ip address
def qpsHost = "CHANGE_ME"

//TODO: generate and replace zafira token
def zafiraAccessToken = "CHANGE_ME"
def qpsPipelineGitURL = "https://github.com/qaprosoft/qps-pipeline.git"
def qpsPipelineGitBranch = "4.5"

def qpsPipelineLogLevel = "INFO"

// def globalPipelineLib = env['GLOBAL_PIPELINE_LIB']


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
    envVars.put("JENKINS_SECURITY_INITIALIZED", "true")
}

// Save the state
instance.save()

