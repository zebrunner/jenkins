# Deploy Zebrunner CE pipelines to 3rd party Jenkins

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
  -> Verify that Manage Jenkins -> Configure Global Security -> Enable script security for Job DSL scripts is unchecked!
* Declare required global variables by [global-args-security.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/manual_deployment/global-args-security.groovy)<br>
  Note: Make sure to replace "CHANGE_ME" infraHost variable  to valid data<br>
  -> Verify that Manage Jenkins -> Configure System has such global variables defined: INFRA_HOST, ZEBRUNNER_PIPELINE, ZEBRUNNER_VERSION, ZEBRUNNER_LOG_LEVEL and JENKINS_SECURITY_INITIALIZED<br>
* Setup Maven installer by [configMavenAutoInstaller.groovy](https://github.com/qaprosoft/jenkins-master/blob/master/resources/init.groovy.d/configMavenAutoInstaller.groovy)<br>
  ->  Verify that Manage Jenkins -> Global Tool Configuration -> Maven installations contains declaration for 'M3'
* Restart Jenkins
  
### Declare Zebrunner-CE library
* Open Manage Jenkins -> Configure System
* Add into the <b>Global Pipeline Libraries</b> Zebrunner-CE entry
  * Name: Zebrunner-CE
  * Default version: master
  * Load implicitly: false
  * Allow default version to be overridden: true
  * Include @Library changes in job recent changes: false
  * Choose "Modern SCM->Git" options
  * Repository URL: https://github.com/zebrunner/pipeline-ce.git
* Save changes

### Adjust Pipeline Speed/Durability Settings
* Open Manage Jenkins -> Configure System
* Specify Pipeline Default Speed/Durability Level: Performance-optimized: much faster (requires clean shutdown to save running pipelines)
* Save changes

### Declare required Global Extensible Choices
* Open Manage Jenkins -> Configure System -> Click "Add New Choice List" and register:
* gc_BUILD_PRIORITY
  * name: gc_BUILD_PRIORITY
  * values: 
    ```
    5
    4
    3
    2
    1
    ```
  * Allow Add Edited Value: false
* gc_CUSTOM_CAPABILITIES
  * name: gc_CUSTOM_CAPABILITIES
  * values: NULL
  * Allow Add Edited Value: false<br>
  Note: specify full list of custom capabilities resource files like [browserstack/android/Samsung_Galaxy_S8.properties](https://github.com/qaprosoft/carina-demo/blob/master/src/main/resources/browserstack/android/Samsung_Galaxy_S8.properties) if needed
* gc_PIPELINE_LIBRARY
  * name: gc_PIPELINE_LIBRARY
  * values: Zebrunner-CE
  * Allow Add Edited Value: true
* gc_RUNNER_CLASS
  * name: gc_RUNNER_CLASS
  * values: 
    ```
    com.zebrunner.jenkins.pipeline.runner.maven.TestNG
    com.zebrunner.jenkins.pipeline.runner.maven.Runner
    com.zebrunner.jenkins.pipeline.runner.gradle.Runner
    ```
  * Allow Add Edited Value: true
* gc_GIT_TYPE
  * name: gc_GIT_TYPE
  * values: 
    ```
    github
    gitlab
    bitbucket
    ```
  * Allow Add Edited Value: true
* Save changes
  
### Create Management Jobs
* Copy recursively [Management_Jobs](https://github.com/zebrunner/jenkins-master/tree/master/resources/jobs/Management_Jobs) to $JENKINS_HOME/jobs<br>
  <b>Warning:</b> for unix based system make sure after copying with sudo permissions to change ownership to exact jenkins user and group, for example:
  ```
  cd /tmp
  git clone https://github.com/zebrunner/jenkins-master.git
  sudo cp -R jenkins-master/resources/jobs/Management_Jobs /var/lib/jenkins/jobs/
  ls -la /var/lib/jenkins/jobs/
  total 12
  drwxr-xr-x  3 jenkins jenkins 4096 Sep 11 10:59 .
  drwxr-xr-x 16 jenkins jenkins 4096 Sep 11 10:50 ..
  drwxr-xr-x  3 root    root    4096 Sep 11 10:59 Management_Jobs
  sudo chown -R jenkins:jenkins /var/lib/jenkins/jobs/
  ```
* Manage Jenkins -> Reload Configuration from Disk
* Verify that folder "Management_Jobs" is created with around 10 default jobs
  
### Test Zebrunner-CE pipeline
* Run Management_Jobs/RegisterOrganization job with parameters:
  ```
  folderName: MyOrganization
  customPipeline: false
  Note: Provide Zebrunner reporting url and token as reportingServiceUrl and reportingAccessToken
  ```
  -> Verify that MyOrganization folder is created at top of your jenkins with launcher and RegisterRepository jobs

* Run MyOrganization/RegisterRepository job with parameters:
  ```
  scmType: github
  pipelineLibrary: Zebrunner-CE
  runnerClass: com.zebrunner.jenkins.pipeline.runner.maven.TestNG
  repoUrl: https://github.com/qaprosoft/carina-demo.git
  branch: master
  scmUser:
  scmToken:
  ```
  -> Verify that inside MyOrganization folder a lot of test jobs created (10+)
* Run API-Demo-Test and Web-Demo-Test jobs with default parameters from  MyOrganization/carina-demo folder<br>
  Note: make sure you have slaves with "api" and "web" labels<br>
  -> Verify that runs are registered successfully in Reporting Tool
  
* Follow [Configuration](https://zebrunner.github.io/community-edition/config-guide/) and [User](https://zebrunner.github.io/community-edition/user-guide/) guides to setup CI/CD process for Test Automation.
