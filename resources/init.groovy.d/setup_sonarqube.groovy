import hudson.model.*
import jenkins.model.*
import hudson.tools.*
import hudson.plugins.sonar.*
import hudson.plugins.sonar.model.TriggersConfig

def env = System.getenv()

// Variables
def sonarName = env['SONAR_NAME']
def sonarUrl = "$SONAR_URL"

// Constants
def instance = Jenkins.getInstance()

Thread.start {
      // SonarQube plugin config
      // source: https://github.com/ridakk/jenkins/blob/master/groovy-scripts/setup-sonarqube-plugin.groovy
      // deleted runner installation as in 5.1 we use maven to execute sonar analysis
      def SonarGlobalConfiguration sonarConfig = instance.getDescriptor(SonarGlobalConfiguration.class)
      def sonar = new SonarInstallation(sonarName, sonarUrl, null, null, '', '', '', '', new TriggersConfig())

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
      
      instance.save()
}
