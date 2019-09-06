import jenkins.model.*
jenkins = Jenkins.instance

def extra_plugins = [

]


def install_extra_plugins(plugins_map){
	def entries = plugins_map.entrySet()
	entries.each { entry ->
	  println "${entry.key}:${entry.value}"
	  def plugin = jenkins.instance.updateCenter.getPlugin(entry.key, new hudson.util.VersionNumber(entry.value))
	  if (plugin != null) {
	    plugin.deploy()
	  } else {
	    println("ERROR! Unable to get plugin ${entry.key}:${entry.value}!")
	  }
	}
}


println "Installing qps-infra plugins required for pure jenkins:"
install_extra_plugins(extra_plugins)