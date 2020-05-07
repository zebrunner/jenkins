import hudson.model.*
import jenkins.model.*
import hudson.tools.*
import hudson.util.Secret
import hudson.plugins.sonar.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import hudson.plugins.sonar.model.TriggersConfig
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl

def env = System.getenv()

// Variables
def sonarName = env['SONAR_NAME']
def sonarUrl = env['SONAR_URL']
def sonarRunnerVersion = env['SONAR_RUNNER_VERSION']
def sonarRunnerName = env['SONAR_RUNNER_NAME']

// Constants
def instance = Jenkins.getInstance()
def global_domain = Domain.global()

def id = "sonar-token"
def desc = "sonar-admin"
def token = env['SONAR_TOKEN']

def credentialsStore = instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

def avilableCredentials = CredentialsProvider.lookupCredentials(StringCredentialsImpl.class, instance, hudson.security.ACL.SYSTEM)
def tokenExists = CredentialsMatchers.firstOrNull(avilableCredentials, CredentialsMatchers.withId(id))

Thread.start {

      if (!tokenExists) {
        def sonarQubeToken = new StringCredentialsImpl(CredentialsScope.GLOBAL, id, desc, Secret.fromString(token))
        credentialsStore.addCredentials(global_domain, sonarQubeToken)
      }
      // SonarQube plugin config
      // source: https://github.com/ridakk/jenkins/blob/master/groovy-scripts/setup-sonarqube-plugin.groovy
      def SonarGlobalConfiguration sonarConfig = instance.getDescriptor(SonarGlobalConfiguration.class)
      def sonar = new SonarInstallation(sonarName, sonarUrl, id, null, '', '', '', '', new TriggersConfig())

      def sonarInstallations = sonarConfig.getInstallations()
      def sonarInstExist = false
      sonarInstallations.each{
        installation = (SonarInstallation) it
        if (sonar.getName() == installation.getName()) {
          sonarInstExist = true
          println("Found existing SonarQube installation: " + installation.getName())
        }
      }
      if (!sonarInstExist) {
        println "--> setting SonarQube plugin"
        sonarInstallations += sonar
        sonarConfig.setInstallations((SonarInstallation[]) sonarInstallations)
        sonarConfig.save()
      }

      def runnerInstDesc = instance.getDescriptor("hudson.plugins.sonar.SonarRunnerInstallation")
      def runnerInstaller = new SonarRunnerInstaller(sonarRunnerVersion)
      def installSourceProperty = new InstallSourceProperty([runnerInstaller])
      def runnerInst = new SonarRunnerInstallation(sonarRunnerName, "", [installSourceProperty])

      def runnerInstallations = runnerInstDesc.getInstallations()
      def runnerInstExists = false
      runnerInstallations.each{
        installation = (SonarRunnerInstallation) it
        if (runnerInst.getName() == installation.getName()) {
          runnerInstExists = true
          println("Found existing SonarRunner installation: " + installation.getName())
        }
      }

      if (!runnerInstExists) {
        println "--> setting up SonarRunner"
        runnerInstallations += runnerInst
        runnerInstDesc.setInstallations((SonarRunnerInstallation[]) runnerInstallations)
        runnerInstDesc.save()
      }

      instance.save()
}
