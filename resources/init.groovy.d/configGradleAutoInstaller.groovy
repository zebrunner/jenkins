import hudson.plugins.gradle.GradleInstallation;
import hudson.tools.InstallSourceProperty;
import hudson.tools.ToolProperty;
import hudson.tools.ToolPropertyDescriptor;
import hudson.util.DescribableList;

def gradleDesc = jenkins.model.Jenkins.instance.getExtensionList(hudson.tasks.gradleGradle)[0]

def isp = new InstallSourceProperty()
def autoInstaller = new hudson.plugins.gradle.GradleInstaller("2.2.1")
isp.installers.add(autoInstaller)

def proplist = new DescribableList<ToolProperty<?>, ToolPropertyDescriptor>()
proplist.add(isp)

def installation = new GradleInstallation("M3", "", proplist)

gradleDesc.setInstallations(installation)
gradleDesc.save()
