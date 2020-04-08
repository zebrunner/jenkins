import hudson.plugins.gradle.GradleInstallation;
import hudson.plugins.gradle.GradleInstaller;
import hudson.tools.InstallSourceProperty;
import hudson.tools.ToolProperty;
import hudson.tools.ToolPropertyDescriptor;
import hudson.util.DescribableList;

def gradleDesc = jenkins.model.Jenkins.instance.getExtensionList(hudson.plugins.gradle.GradleInstallation.DescriptorImpl.class)[0]

def installSourceProperty = new InstallSourceProperty()

def autoInstaller = new GradleInstaller("6.3")
installSourceProperty.installers.add(autoInstaller)

def proplist = new DescribableList<ToolProperty<?>, ToolPropertyDescriptor>()
proplist.add(installSourceProperty)

def installation = new GradleInstallation("G6", "", proplist)

gradleDesc.setInstallations(installation)
gradleDesc.save()
