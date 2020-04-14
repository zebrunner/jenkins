# Dockerized Jenkins master

### How to run
```
docker run -p 8080:8080 -p 50000:50000 \
          -v ~/jenkins_home:/var/jenkins_home -v ~/.m2:/root/.m2 -v ~/.ssh:/var/jenkins_home/.ssh \
          -e JENKINS_OPTS="--prefix=/jenkins --httpPort=8080" \
          -d --rm --name jenkins qaprosoft/jenkins-master:latest
```
Note: shared folder $HOME/jenkins_home, $HOME/.m2 and $HOME/.ssh must exist and belong to your current user. Use "chown" to fix it if neccessary.

### How to authorize
1. Open http://hostname:8080/jenkins
2. Login using admin/qaprosoft credentials

### How to use
Follow configuration guide in [qps-infra](https://qaprosoft.github.io/qps-infra) to reuse Jenkins effectively for automation.

## Manual deployment steps for 3rd party Jenkins Setup
In order to configure existing Jenkins with automation Pipeline/JobDSL follow detailed [guide](https://github.com/qaprosoft/jenkins-master/blob/master/manual_deployment/README.md)

## F.A.Q
Q: Unable to start any job due to the:
```
General error during conversion: Error grabbing Grapes -- [download failed: org.beanshell#bsh;2.0b4!bsh.jar]
```
A: remove completely $HOME/.m2/repository and QPS_HONE/jenkins/.groovy/grapes content to allow jenkins to redownload everything from scratch
```
rm -rf ~/.m2/repository
cd ~/tools/qps-infra
rm -rf ./jenkins/.groovy/grapes
```
