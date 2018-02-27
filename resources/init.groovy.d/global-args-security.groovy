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

def user = env['ADMIN_USER']
def pass = env['ADMIN_PASS']

def coreLogLevel = env['CORE_LOG_LEVEL']
def seleniumHost = env['SELENIUM_HOST']
def carinaCoreVersion = env['CARINA_CORE_VERSION']
def zafiraBaseConfig = env['ZAFIRA_BASE_CONFIG']
def zafiraServiceURL = env['ZAFIRA_SERVICE_URL']
def zafiraAccessToken = env['ZAFIRA_ACCESS_TOKEN']
def jenkinsPipelineGitURL = env['JENKINS_PIPELINE_GIT_URL']
def jenkinsPipelineGitBranch = env['JENKINS_PIPELINE_GIT_BRANCH']

def gitApiURL = env['GITHUB_API_URL']
def gitHost = env['GITHUB_HOST']
def gitHtmlURL = env['GITHUB_HTML_URL']
def gitOauthToken = env['GITHUB_OAUTH_TOKEN']
def gitOrganization = env['GITHUB_ORGANIZATION']
def gitSshURL = env['GITHUB_SSH_URL']

def jacocoBucket = env['JACOCO_BUCKET']
def jacocoEnable = env['JACOCO_ENABLE']

def jobMaxRunTime = env['JOB_MAX_RUN_TIME']

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

    if ( jenkinsPipelineGitURL != null && !envVars.containsKey("JENKINS_PIPELINE_GIT_URL") ) {
      envVars.put("JENKINS_PIPELINE_GIT_URL", jenkinsPipelineGitURL)
    }

    if ( jenkinsPipelineGitBranch != null && !envVars.containsKey("JENKINS_PIPELINE_GIT_BRANCH") ) {
      envVars.put("JENKINS_PIPELINE_GIT_BRANCH", jenkinsPipelineGitBranch)
    }

    if (gitApiURL != null && !envVars.containsKey("GITHUB_API_URL") ) {
      envVars.put("GITHUB_API_URL", gitApiURL)
    }

    if (gitHost != null && !envVars.containsKey("GITHUB_HOST") ) {
      envVars.put("GITHUB_HOST", gitHost)
    }

    if (gitHtmlURL != null && !envVars.containsKey("GITHUB_HTML_URL") ) {
      envVars.put("GITHUB_HTML_URL", gitHtmlURL)
    }

    if (gitOauthToken != null && !envVars.containsKey("GITHUB_OAUTH_TOKEN") ) {
      envVars.put("GITHUB_OAUTH_TOKEN", gitOauthToken)
    }

    if (gitOrganization != null && !envVars.containsKey("GITHUB_ORGANIZATION") ) {
      envVars.put("GITHUB_ORGANIZATION", gitOrganization)
    }

    if (gitSshURL != null && !envVars.containsKey("GITHUB_SSH_URL") ) {
      envVars.put("GITHUB_SSH_URL", gitSshURL)
    }

    if (jobMaxRunTime != null && !envVars.containsKey("JOB_MAX_RUN_TIME") ) {
      envVars.put("JOB_MAX_RUN_TIME", jobMaxRunTime)
    }

    if ( jacocoBucket != null && !envVars.containsKey("JACOCO_BUCKET") ) {
      envVars.put("JACOCO_BUCKET", jacocoBucket)
    }

    if ( jacocoEnable != null && !envVars.containsKey("JACOCO_ENABLE") ) {
      envVars.put("JACOCO_ENABLE", jacocoEnable)
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

    // Save the state
    instance.save()
}
