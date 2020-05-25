# Deploy qps-infra setting to 3rd party Jenkins

### Prerequisites
* Jenkins instance URL
* Jenkins Admin credentials
* Jenkins java option on startup: <i>-Dhudson.model.ParametersAction.keepUndefinedParameters=true</i>
  Note: Option needed starting from 4.8+ jenkins-master. Example: [link](https://github.com/qaprosoft/qps-infra/commit/4bddc573d5296150d2de39cff4ebb2a60be9895f)

### Install required plugins
* Open Manage Jenkins -> Script Console
* Execute [install_plugins.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/manual_deployment/install_plugins.groovy) script manually
* Restart Jenkins

### Finish configuration steps (via Manage Jenkins -> Script Console)
* Disable scipt security for JobDSL to enable additional classpath for Pipeline+JobDSL steps [disable-scripts-security-for-job-dsl-scripts.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/resources/init.groovy.d/disable-scripts-security-for-job-dsl-scripts.groovy)<br>
  Note: for details visit https://issues.jenkins-ci.org/browse/JENKINS-40961 and https://github.com/jenkinsci/job-dsl-plugin/wiki/Migration#migrating-to-160<br>
  <b>Warning:</b> Please, verify that Manage Jenkins -> Configure Global Security -> Enable script security for Job DSL scripts is unchecked!
* Declare required global variables by [global-args-security.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/manual_deployment/global-args-security.groovy)<br>
  -> Verify that Manage Jenkins -> Configure System has such golbal variables defined: ADMIN_EMAILS, JENKINS_SECURITY_INITIALIZED, INFRA_HOST, QPS_PIPELINE_GIT_BRANCH, QPS_PIPELINE_GIT_URL, QPS_PIPELINE_LOG_LEVEL<br>
  Note: Make sure to replace "CHANGE_ME" variables values onto the valid data
* Declare required aws-jacoco-token [setup_aws_credentials.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/resources/init.groovy.d/setup_aws_credentials.groovy)<br>
  Note: valid value can be added manually if needed
* Setup Maven installer by [configMavenAutoInstaller.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/resources/init.groovy.d/configMavenAutoInstaller.groovy) 
  ->  Verify that Manage Jenkins  -> Global Tool Configuration     -> Maven installations contains declaration for 'M3'
* Setup SBT installerby [configSbtAutoInstaller.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/resources/init.groovy.d/configSbtAutoInstaller.groovy) 
  -> Verify that Manage Jenkins -> Global Tool Configuration -> Sbt installations contains declaration for 'SBT'
* Setup GitHub Pull Request Builder by [setup_ghprbhook_credentials.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/manual_deployment/setup_ghprbhook_credentials.groovy)-> verify that Manage Jenkins -> Global Tool Configuration -> Setup GitHub Pull Request Builder credential field contains 'ghprbhook-token'
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
* Click "Add New Choice List"
  * name: gc_BUILD_PRIORITY
  * values: 5, 4, 3, 2, 1
  Note: Each value from this doc should be specified in new line!
* Click "Add New Choice List"
  * name: gc_CUSTOM_CAPABILITIES
  * values: NULL
  Note: specify full list of custom capabilities resource file like https://github.com/qaprosoft/carina-demo/tree/master/src/main/resources/browserstack/android
  For example: browserstack/android/Samsung_Galaxy_S8.properties
* Save changes
  
### Create Management Jobs
* Copy recursively [Management_Jobs](https://github.com/qaprosoft/jenkins-master/tree/plugins/resources/jobs/Management_Jobs) to $JENKINS_HOME/jobs
  <b>Warning:</b> for unix based system make sure after copying with sudo permissions to change ownership to exact jenkins user and group, for example:
  ```
  cd /tmp
  git clone https://github.com/qaprosoft/jenkins-master.git
  sudo cp -R jenkins-master/resources/jobs/Management_Jobs /var/lib/jenkins/jobs/
  ls -la /var/lib/jenkins/jobs/
  total 12
  drwxr-xr-x  3 jenkins jenkins 4096 Sep 11 10:59 .
  drwxr-xr-x 16 jenkins jenkins 4096 Sep 11 10:50 ..
  drwxr-xr-x  3 root    root    4096 Sep 11 10:59 Management_Jobs
  sudo chown -R jenkins:jenkins /var/lib/jenkins/jobs/
  ```
* Manage Jenkins -> Reload Configuration from Disk
* Verify that folder "Management_Jobs" is created (4-5 default jobs should present as of today)
  
### Test deployment steps
* Run Management_Jobs/RegisterOrganization job with parameters:
  folderName: folderName
  pipelineLibrary: QPS-Pipeline
  runnerClass: com.qaprosoft.jenkins.pipeline.runner.maven.QARunner
  securityEnabled: false
  Note: verify that qaprosoft folder is created at top of your jenkins with launcher and registerRepository jobs
* Run qaprosoft/RegisterRepository job with parameters:<br>
   organization: qaprosoft<br>
   repo: carina-demo<br>
   branch: master<br>
   githubUser:<br>
   githubToken:<br>
   pipelineLibrary: QPS-Pipeline<br>
   runnerClass: com.qaprosoft.jenkins.pipeline.runner.maven.QARunner<br>
  Note: verify that inside qaprosoft folder a lot of test jobs created (10+)
* Run qaprosoft/API-Demo-Test job with default parameters<br>
  Note: make sure you have slave with "api" label to be able to run tests
* Run qaprosoft/Web-Demo-Test job with default parameters<br>
  Note: make sure you have slave with "web" label to be able to run tests
* verify that runs are registered successfully in Reporting Tool
  
