import hudson.tools.InstallSourceProperty
import hudson.tools.ToolProperty
import hudson.tools.ToolPropertyDescriptor
import hudson.util.DescribableList
import org.jvnet.hudson.plugins.SbtPluginBuilder.SbtInstaller
import org.jvnet.hudson.plugins.SbtPluginBuilder.SbtInstallation

def sbtDesc = jenkins.model.Jenkins.instance.getExtensionList(org.jvnet.hudson.plugins.SbtPluginBuilder.DescriptorImpl.class)[0]

def installSourceProperty = new InstallSourceProperty()

def autoInstaller = new SbtInstaller("1.2.8")
installSourceProperty.installers.add(autoInstaller)

def proplist = new DescribableList<ToolProperty<?>, ToolPropertyDescriptor>()
proplist.add(installSourceProperty)

def installation = new SbtInstallation("SBT", "", "", proplist)

sbtDesc.setInstallations(installation)
sbtDesc.save()
