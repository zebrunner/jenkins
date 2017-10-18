import hudson.model.*;
import jenkins.model.*;

// Variables
def env = System.getenv()

def rootURL = env['ROOT_URL']
def rootEmail = env['ROOT_EMAIL']

def carinaCoreVersion = env['CARINA_CORE_VERSION']
def coreLogLevel = env['CORE_LOG_LEVEL']
def defaultBaseMavenGoals = env['DEFAULT_BASE_MAVEN_GOALS']
def seleniumHost = env['SELENIUM_HOST']
def zafiraBaseConfig = env['ZAFIRA_BASE_CONFIG']
def zafiraServiceURL = env['ZAFIRA_SERVICE_URL']

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

    if ( carinaCoreVersion != null ) {
      envVars.put("CARINA_CORE_VERSION", carinaCoreVersion)
    }

    if ( coreLogLevel != null ) {
      envVars.put("CORE_LOG_LEVEL", coreLogLevel)
    }

    if ( defaultBaseMavenGoals != null ) {
      envVars.put("DEFAULT_BASE_MAVEN_GOALS", defaultBaseMavenGoals)
    }

    if ( seleniumHost != null ) {
      envVars.put("SELENIUM_HOST", seleniumHost)
    }

    if ( zafiraBaseConfig != null ) {
      envVars.put("ZAFIRA_BASE_CONFIG", zafiraBaseConfig)
    }

    if ( zafiraServiceURL != null ) {
      envVars.put("ZAFIRA_SERVICE_URL", zafiraServiceURL)
    }

    if ( globalPipelineLib != null ) {
      envVars.put("GLOBAL_PIPELINE_LIB", globalPipelineLib)
    }

    // Save the state
    instance.save()
}
