import hudson.plugins.gradle.GradleInstallation;
import hudson.plugins.gradle.GradleInstaller;
import hudson.tools.InstallSourceProperty;
import hudson.tools.ToolProperty;
import hudson.tools.ToolPropertyDescriptor;
import hudson.util.DescribableList;

def gradleDesc = jenkins.model.Jenkins.instance.getExtensionList(hudson.plugins.gradle.Gradle)[0]

def isp = new InstallSourceProperty()
def autoInstaller = new GradleInstaller("6.2.2")
isp.installers.add(autoInstaller)

def proplist = new DescribableList<ToolProperty<?>, ToolPropertyDescriptor>()
proplist.add(isp)

def installation = new GradleInstallation("G6", "", proplist)

gradleDesc.setInstallations(installation)
gradleDesc.save()
