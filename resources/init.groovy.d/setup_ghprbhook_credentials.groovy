import hudson.model.*;
import jenkins.model.*
import java.lang.reflect.Field
import org.jenkinsci.plugins.ghprb.GhprbGitHubAuth
import hudson.util.Secret
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*

println "--> setting ghprhook creds"

// Constants
def instance = Jenkins.getInstance()

def global_domain = Domain.global()

def credentialsStore =
        Jenkins.instance.getExtensionList(
                'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
        )[0].getStore()

def env = System.getenv()
def id = "ghprbhook-token"
def username = "CHANGE_ME"
def password = "CHANGE_ME"
def description = "GitHub Pull Request Builder token"

def ghprbhookCredentials = new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        id,
        description,
        username,
        password
)

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


if(!envVars.containsKey("JENKINS_SECURITY_INITIALIZED") || envVars.get("JENKINS_SECURITY_INITIALIZED") != "true") {
  credentialsStore.addCredentials(global_domain, ghprbhookCredentials)
  def descriptor = Jenkins.instance.getDescriptorByType(org.jenkinsci.plugins.ghprb.GhprbTrigger.DescriptorImpl.class)
  Field auth = descriptor.class.getDeclaredField("githubAuth")
  auth.setAccessible(true)
  def githubAuth = new ArrayList<GhprbGitHubAuth>(1)

  Secret secret = Secret.fromString('')
  githubAuth.add(new GhprbGitHubAuth("https://api.github.com", "", id, description, username, secret))
  auth.set(descriptor, githubAuth)

  descriptor.save()
}
