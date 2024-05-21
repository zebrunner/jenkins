FROM jenkins/jenkins:2.452.1-lts-jdk11

ENV ROOT_URL=http://localhost:8080/jenkins
ENV ROOT_EMAIL=qps-auto@zebrunner.com
ENV ADMIN_EMAILS=qps-auto@zebrunner.com
ENV ADMIN_USER=admin
ENV ADMIN_PASS=changeit
ENV GENERIC_WEBHOOK_SECRET=CHANGE_ME
ENV INFRA_HOST=localhost
ENV ZEBRUNNER_PIPELINE=https://github.com/zebrunner/pipeline-ce.git
ENV ZEBRUNNER_VERSION=1.1
ENV ZEBRUNNER_LOG_LEVEL=INFO
ENV JENKINS_OPTS="--prefix=/jenkins --httpPort=8080 --httpsPort=8443 --httpsKeyStore=/var/jenkins_home/keystore.jks --httpsKeyStorePassword=password"
ENV JAVA_OPTS="-Dhudson.model.ParametersAction.keepUndefinedParameters=true"
ENV AWS_KEY=CHANGE_ME
ENV AWS_SECRET=CHANGE_ME
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US.UTF-8
ENV SONAR_URL=

USER root

COPY resources/healthcheck /usr/local/bin/

# Install Git

# RUN apk update && apk upgrade && \
#    apk add --no-cache bash git openssh

# Install utils
RUN apt-get update && \
	apt-get install -qqy iputils-ping telnet nano procps netcat-openbsd iputils-ping rsync

# Initialize Jenkins

USER jenkins

RUN echo $JENKINS_VERSION > /usr/share/jenkins/ref/jenkins.install.UpgradeWizard.state \
	&& echo $JENKINS_VERSION > /usr/share/jenkins/ref/jenkins.install.InstallUtil.lastExecVersion

COPY resources/init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/
COPY resources/jobs/ /usr/share/jenkins/ref/jobs/

# Copy plugins list
COPY resources/configs/plugins.txt /usr/share/jenkins/ref/

# Copy default keystore.jks with self-signed localhost certificate
COPY resources/ssl/keystore.jks /var/jenkins_home

# override default jenkins.sh to be able to upload extra plugins on startup
COPY resources/scripts/jenkins.sh /usr/local/bin/jenkins.sh

COPY resources/configs/jp.ikedam.jenkins.plugins.extensible_choice_parameter.GlobalTextareaChoiceListProvider.xml /usr/share/jenkins/ref/
COPY resources/configs/org.jenkinsci.plugins.workflow.libs.GlobalLibraries.xml /usr/share/jenkins/ref/

HEALTHCHECK CMD ["healthcheck"]
