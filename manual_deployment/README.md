# Deploy qps-infra setting to 3rd party Jenkins

### Prerequisites
* Jenkins instance deployed somehow on your premises environment (Linux OS is highly recommended)
* Admin privileges to the installed Jenkins


### Install required plugins
* Open Manage Jenkins -> Script Console
* Execute [install_plugins.groovy](https://github.com/qaprosoft/jenkins-master/plugins/manual_deployment/install_plugins.groovy) script manually
* Restart Jenkins

### Finish configuration steps
* Run below scripts from manage Jenkins -> Script Console
  * Disable scipt security for JobDSL to enable additional classpath for Pipeline+JobDSL steps [disable-scripts-security-for-job-dsl-scripts.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/resources/init.groovy.d/disable-scripts-security-for-job-dsl-scripts.groovy)<br>
  Note: for details visit https://issues.jenkins-ci.org/browse/JENKINS-40961 and https://github.com/jenkinsci/job-dsl-plugin/wiki/Migration#migrating-to-160
  * Declare required global variables by [global-args-security.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/resources/init.groovy.d/global-args-security.groovy)
  * Declare required aws-jacoco-token [setup_aws_credentials.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/resources/init.groovy.d/setup_aws_credentials.groovy)<br>
  Note: valid value can be added manually if needed
  * Apply optimization rules by [tcp-slave-agent-port.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/resources/init.groovy.d/tcp-slave-agent-port.groovy)
  * Setup Maven installer by [configMavenAutoInstaller.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/resources/init.groovy.d/configMavenAutoInstaller.groovy) 
  * Setup SBT installerby [configSbtAutoInstaller.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/resources/init.groovy.d/configSbtAutoInstaller.groovy) 
  * Restart Jenkins
  
### Declare QPS-Pipeline library
* Open Manage Jenkins -> Configure System
* Add into the <b>Global Pipeline Libraries</b> QPS-Pipeline entry
  * Name: QPS-Pipeline
  * Default version: master
  * Load implicitly: false
  * Allow default version to be overridden: true
  * Include @Library changes in job recent changes: false
  * Choose "Legacy SCM->Git" options
  * Repository URL: ${QPS_PIPELINE_GIT_URL}
  * Credentials: none
  * Branches to build: ${QPS_PIPELINE_GIT_BRANCH}
  * Repository browser: (Auto)
  * Additional Behaviours: none
