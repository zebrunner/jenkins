# Deploy qps-infra setting to 3rd party Jenkins

### TODO: Review steps
Update links onto the groovy etc scripts from plugins to master branch after merging all those changes

### Prerequisites
* Jenkins instance deployed somehow on your premises environment (Linux OS is highly recommended)
* Admin privileges to the installed Jenkins


### Install required plugins
* Open Manage Jenkins -> Script Console
* Execute [install_plugins.groovy](https://github.com/qaprosoft/jenkins-master/blob/plugins/manual_deployment/install_plugins.groovy) script manually
* Restart Jenkins

### Finish configuration steps (via Mnage Jenkins -> Script Console)
* Disable scipt security for JobDSL to enable additional classpath for Pipeline+JobDSL steps [disable-scripts-security-for-job-dsl-scripts.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/resources/init.groovy.d/disable-scripts-security-for-job-dsl-scripts.groovy)<br>
  Note: for details visit https://issues.jenkins-ci.org/browse/JENKINS-40961 and https://github.com/jenkinsci/job-dsl-plugin/wiki/Migration#migrating-to-160
  <b>Warning:</b> Please, verify that Manage Jenkins -> Configure Global Security -> Enable script security for Job DSL scripts is unchcked!<br>
* Declare required global variables by [global-args-security.groovy](https://github.com/qaprosoft/jenkins-master/blob/plugins/manual_deployment/global-args-security.groovy)
  Note: Verify that Manage Jenkins -> Configure System has such golbal variables defined: ADMIN_EMAILS, JENKINS_SECURITY_INITIALIZED, QPS_HOST, QPS_PIPELINE_GIT_BRANCH, QPS_PIPELINE_GIT_URL, QPS_PIPELINE_LOG_LEVEL, ZAFIRA_ACCESS_TOKEN
  <b>Warning:</b> Make sure to replace "CHANGE_ME" variables values onto the valid data
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
* Save changes

![Alt text](./qps-pipeline-library.png?raw=true "QPS-Pipeline library")

### Adjust Pipeline Speed/Durability Settings
* Open Manage Jenkins -> Configure System
* Specify Pipeline Default Speed/Durability Level: Performance-optimized: much faster (requires clean shutdown to save running pipelines)
* Save changes

### Declare required Global Extensible Choices
* Open Manage Jenkins -> Configure System
* Click "Add New Choice List" in Extensible Choice: Available Choice Providers
  * name: gc_GIT_BRANCH
  * values: master
* Click "Add New Choice List"
  * name: gc_BUILD_PRIORITY
  * values: 5, 4, 3, 2, 1
  Note: Each value from this doc should be specified in new line!
* Click "Add New Choice List"
  * name: gc_BROWSER
  * values: chrome, firefox
  Note: safari and ie can be added if your infrastructure support them
* Click "Add New Choice List"
  * name: gc_CUSTOM_CAPABILITIES
  * values: NULL
  Note: specify full list of custom capabilities resource file like https://github.com/qaprosoft/carina-demo/tree/master/src/main/resources/browserstack/android
  For example: browserstack/android/Samsung_Galaxy_S8.properties
* Save changes
  
### Create Management Jobs
* Copy [Management_Jobs](https://github.com/qaprosoft/jenkins-master/tree/plugins/resources/jobs/Management_Jobs) to $JENKINS_HOME/jobs
* Manage Jenkins -> Reload Configuration from Disk
* Verify that folder "Management_Jobs" is created (4-5 default jobs shold present as of today)
  


  
  
