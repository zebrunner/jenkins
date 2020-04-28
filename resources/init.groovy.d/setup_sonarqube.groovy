import hudson.model.*
import jenkins.model.*
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.TriggersConfig
import hudson.tools.*

def env = System.getenv()

// Variables
def sonarName = env['SONAR_NAME']
def sonarUrl = env['SONAR_URL']
def sonarUser = env['SONAR_USER']
def sonarPassword = env['SONAR_PASS']
def sonarRunnerName = env['SONAR_RUNNER_NAME']
def sonarRunnerVersion = env['SONAR_RUNNER_VERSION']

// Constants
def instance = Jenkins.getInstance()

Thread.start {
      // SonarQube plugin config
      // source: https://github.com/ridakk/jenkins/blob/master/groovy-scripts/setup-sonarqube-plugin.groovy
      println "--> setting SonarQube plugin"
      def SonarGlobalConfiguration sonarConfig = instance.getDescriptor(SonarGlobalConfiguration.class)
      def sonar = new SonarInstallation(sonarName, sonarUrl, sonarName + sonarPassword, '', '', new TriggersConfig(), '')

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
        sonarInstallations += sonar
        sonarConfig.setInstallations((SonarInstallation[]) sonarInstallations)
        sonarConfig.save()
      }

      println "--> setting up SonarRunner"
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
        runnerInstallations += runnerInst
        runnerInstDesc.setInstallations((SonarRunnerInstallation[]) runnerInstallations)
        runnerInstDesc.save()
      }

      instance.save()
}
