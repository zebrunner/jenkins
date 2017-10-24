import hudson.model.*;
import jenkins.model.*;

// Disable Jenkins security that blocks eTAF reports
System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "default-src 'self'; script-src 'self' https://ajax.googleapis.com 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self'")

// Variables
def env = System.getenv()

def rootURL = env['ROOT_URL']
def rootEmail = env['ROOT_EMAIL']

def coreLogLevel = env['CORE_LOG_LEVEL']
def defaultBaseMavenGoals = env['DEFAULT_BASE_MAVEN_GOALS']
def seleniumHost = env['SELENIUM_HOST']
def carinaCoreVersion = env['CARINA_CORE_VERSION']
def zafiraBaseConfig = env['ZAFIRA_BASE_CONFIG']
def zafiraServiceURL = env['ZAFIRA_SERVICE_URL']
def zafiraAccessToken = env['ZAFIRA_ACCESS_TOKEN']
def jenkinsJobDslGitURL = env['JENKINS_JOB_DSL_GIT_URL']

def globalPipelineLib = env['GLOBAL_PIPELINE_LIB']

// Constants
def instance = Jenkins.getInstance()

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

    if ( coreLogLevel != null && !envVars.containsKey("CORE_LOG_LEVEL") ) {
      envVars.put("CORE_LOG_LEVEL", coreLogLevel)
    }

    if ( defaultBaseMavenGoals != null && !envVars.containsKey("DEFAULT_BASE_MAVEN_GOALS") ) {
      envVars.put("DEFAULT_BASE_MAVEN_GOALS", defaultBaseMavenGoals)
    }

    if ( seleniumHost != null && !envVars.containsKey("SELENIUM_HOST") ) {
      envVars.put("SELENIUM_HOST", seleniumHost)
    }

    if ( carinaCoreVersion != null && !envVars.containsKey("CARINA_CORE_VERSION") ) {
      envVars.put("CARINA_CORE_VERSION", carinaCoreVersion)
    }

    if ( zafiraBaseConfig != null && !envVars.containsKey("ZAFIRA_BASE_CONFIG") ) {
      envVars.put("ZAFIRA_BASE_CONFIG", zafiraBaseConfig)
    }

    if ( zafiraServiceURL != null && !envVars.containsKey("ZAFIRA_SERVICE_URL") ) {
      envVars.put("ZAFIRA_SERVICE_URL", zafiraServiceURL)
    }

    if ( zafiraAccessToken != null && !envVars.containsKey("ZAFIRA_ACCESS_TOKEN") ) {
      envVars.put("ZAFIRA_ACCESS_TOKEN", zafiraAccessToken)
    }

    if ( globalPipelineLib != null && !envVars.containsKey("GLOBAL_PIPELINE_LIB") ) {
      envVars.put("GLOBAL_PIPELINE_LIB", globalPipelineLib)
    }

    if ( jenkinsJobDslGitURL != null && !envVars.containsKey("JENKINS_JOB_DSL_GIT_URL") ) {
      envVars.put("JENKINS_JOB_DSL_GIT_URL", jenkinsJobDslGitURL)
    }

    // Save the state
    instance.save()
}
