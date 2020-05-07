FROM jenkins/jenkins:2.234-alpine

ENV ROOT_URL=http://localhost:8080/jenkins
ENV ROOT_EMAIL=qps-auto@qaprosoft.com
ENV ADMIN_EMAILS=qps-auto@qaprosoft.com
ENV ADMIN_USER=admin
ENV ADMIN_PASS=qaprosoft
ENV GHPRBHOOK_USER=CHANGE_ME
ENV GHPRBHOOK_PASS=CHANGE_ME
ENV INFRA_HOST=localhost
ENV QPS_PIPELINE_GIT_URL=https://github.com/qaprosoft/qps-pipeline.git
ENV QPS_PIPELINE_GIT_BRANCH=develop
ENV JENKINS_OPTS="--prefix=/jenkins --httpPort=8080"
ENV JAVA_OPTS="-Dhudson.model.ParametersAction.keepUndefinedParameters=true"
ENV AWS_KEY=CHANGE_ME
ENV AWS_SECRET=CHANGE_ME
ENV QPS_PIPELINE_LOG_LEVEL=INFO
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US.UTF-8
ENV SONAR_NAME=sonar-ci
ENV SONAR_URL=http://sonarqube:9000/sonarqube
ENV SONAR_TOKEN=CHANGE_ME
ENV SONAR_RUNNER_NAME=sonar-ci-scanner
ENV SONAR_RUNNER_VERSION=4.3.0.2102


USER root

# Install Git

# RUN apk update && apk upgrade && \
#    apk add --no-cache bash git openssh

# Install net utils
RUN apk add --update --no-cache bind-tools busybox-extras

#======================
# Install Apache Maven
#======================

ARG MAVEN_VERSION=3.6.3
ARG SHA=26ad91d751b3a9a53087aefa743f4e16a17741d3915b219cf74112bf87a438c5
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha256sum -c - \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "/var/jenkins_home/.m2"

COPY resources/scripts/mvn-entrypoint.sh /usr/local/bin/mvn-entrypoint.sh
COPY resources/configs/settings-docker.xml /usr/share/maven/ref/

RUN chown -R jenkins /usr/share/maven /usr/share/maven/ref
RUN chmod a+w /etc/ssl/certs/java/cacerts

RUN /usr/local/bin/mvn-entrypoint.sh

# Initialize Jenkins

USER jenkins

COPY resources/init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/
COPY resources/jobs/ /usr/share/jenkins/ref/jobs/

# Configure plugins
COPY resources/configs/plugins.txt /usr/share/jenkins/ref/
RUN /usr/local/bin/install-plugins.sh $(cat /usr/share/jenkins/ref/plugins.txt | tr '\n' ' ')

# override default jenkins.sh to be able to upload extra plugins on startup
COPY resources/scripts/jenkins.sh /usr/local/bin/jenkins.sh

COPY resources/configs/jp.ikedam.jenkins.plugins.extensible_choice_parameter.GlobalTextareaChoiceListProvider.xml /usr/share/jenkins/ref/
COPY resources/configs/org.jenkinsci.plugins.workflow.libs.GlobalLibraries.xml /usr/share/jenkins/ref/
