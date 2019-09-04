import jenkins.model.Jenkins

def not_install_plugins = [
'ace-editor':'1.1',
'amazon-ecs':'1.22',
'analysis-core':'1.96',
'azure-commons':'1.0.4',
'blueocean-autofavorite':'1.2.4',
'blueocean-bitbucket-pipeline':'1.18.1',
'blueocean-commons':'1.18.1',
'blueocean-config':'1.18.1',
'blueocean-core-js':'1.18.1',
'blueocean-dashboard':'1.18.1',
'blueocean-display-url':'2.3.0',
'blueocean-events':'1.18.1',
'blueocean-git-pipeline':'1.18.1',
'blueocean-github-pipeline':'1.18.1',
'blueocean-i18n':'1.18.1',
'blueocean-jira':'1.18.1',
'blueocean-jwt':'1.18.1',
'blueocean-personalization':'1.18.1',
'blueocean-pipeline-api-impl':'1.18.1',
'blueocean-pipeline-editor':'1.18.1',
'blueocean-pipeline-scm-api':'1.18.1',
'blueocean-rest-impl':'1.18.1',
'blueocean-rest':'1.18.1',
'blueocean-web':'1.18.1',
'blueocean':'1.18.1',
'build-pipeline-plugin':'1.5.8',
'built-on-column':'1.1',
'cloudbees-bitbucket-branch-source':'2.4.6',
'cobertura':'1.14',
'code-coverage-api':'1.0.13',
'conditional-buildstep':'1.3.6',
'config-file-provider':'3.6.2',
'copyartifact':'1.42.1',
'cvs':'2.14',
'database-postgresql':'1.0',
'database':'1.5',
'docker-build-step':'2.3',
'dynamic_extended_choice_parameter':'1.0.1',
'ec2-fleet':'1.10.2',
'envinject-api':'1.6',
'envinject':'2.2.0',
'favorite':'2.3.2',
'gatling':'1.2.6',
'github-branch-source':'2.5.6',
'groovy':'2.2',
'h2-api':'1.4.199',
'handy-uri-templates-2-api':'2.1.7-1.0',
'http_request':'1.8.23',
'javadoc':'1.5',
'jenkins-design-language':'1.18.1',
'jenkins-multijob-plugin':'1.32',
'jira':'3.0.9',
'jquery-ui':'1.0.2',
'jquery':'1.12.4-1',
'junit-attachments':'1.6',
'kubernetes-cd':'2.1.1',
'kubernetes-credentials':'0.4.1',
'kubernetes':'1.18.2',
'matrix-auth':'2.4.2',
'mercurial':'2.8',
'nodelabelparameter':'1.7.2',
'parameterized-trigger':'2.35.2',
'pipeline-aws':'1.38',
'pipeline-maven':'3.8.1',
'pipeline-utility-steps':'2.3.0',
'pubsub-light':'1.13',
'rebuild':'1.31',
'role-strategy':'2.13',
'run-condition':'1.2',
's3':'0.11.2',
'saml':'1.1.2',
'slack':'2.32',
'sonar':'2.9',
'sse-gateway':'1.19',
'ssh-agent':'1.17',
'swarm':'3.17',
'tasks':'4.53',
'trilead-api':'1.0.5',
'uno-choice':'2.1'
]


def install_plugins(plugins_map){
	def entries = plugins_map.entrySet()
	entries.each { entry ->
	  println "${entry.key}:${entry.value}"
	  def plugin = Jenkins.instance.updateCenter.getPlugin(entry.key, new hudson.util.VersionNumber(entry.value))
	  if (plugin != null) {
	    plugin.deploy()
	  } else {
	    println("ERROR! Unable to get plugin ${entry.key}:${entry.value}!")
	  }
	}
}


println "Installing qps-infra plugins required for pure jenkins:"
install_plugins(not_install_plugins)
