FROM jenkins/jenkins:2.163-alpine

ENV ROOT_URL=http://localhost:8083/jenkins
ENV ROOT_EMAIL=qps-auto@qaprosoft.com
ENV ADMIN_EMAILS=qps-auto@qaprosoft.com
ENV ADMIN_USER=admin
ENV ADMIN_PASS=qaprosoft
ENV QPS_HOST=localhost
ENV QPS_PIPELINE_GIT_URL=https://github.com/qaprosoft/qps-pipeline.git
ENV QPS_PIPELINE_GIT_BRANCH=3.12
ENV JENKINS_OPTS="--prefix=/jenkins --httpPort=-1 --httpsPort=8083 --httpsKeyStore=/var/jenkins_home/keystore.jks --httpsKeyStorePassword=password"
ENV ZAFIRA_ACCESS_TOKEN=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwicGFzc3dvcmQiOiJ6WHRoVy9CNS9CZk9QRW4xRktMVy8vbmpqbkZFSGJhZSIsInRlbmFudCI6InphZmlyYSIsImV4cCI6MTMwMzg2OTAyMzg3fQ.8xDrHUmtahzBrbyKrAX-Xkr9cUZXpfH5aC-rDh1oQZGWCfVME76YEsUPPoozOOfhHKg6AzV56w-BEq9UtGz_AA
ENV AWS_KEY=CHANGE_ME
ENV AWS_SECRET=CHANGE_ME
ENV QPS_PIPELINE_LOG_LEVEL=INFO

USER root

# Install Git

RUN apk update && apk upgrade && \
    apk add --no-cache bash git openssh


# Install Apache Maven

ARG MAVEN_VERSION=3.5.4
ARG USER_HOME_DIR="/root"
ARG SHA=ce50b1c91364cb77efe3776f756a6d92b76d9038b0a0782f7d53acf1e997a14d
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha256sum -c - \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

COPY resources/scripts/mvn-entrypoint.sh /usr/local/bin/mvn-entrypoint.sh
COPY resources/configs/settings-docker.xml /usr/share/maven/ref/

VOLUME "$USER_HOME_DIR/.m2"

RUN chown -R jenkins "$USER_HOME_DIR" /usr/share/maven /usr/share/maven/ref
RUN chmod a+w /etc/ssl/certs/java/cacerts

RUN /usr/local/bin/mvn-entrypoint.sh

# Initialize Jenkins

USER jenkins

COPY resources/init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/
COPY resources/jobs/ /usr/share/jenkins/ref/jobs/

# Configure plugins

COPY resources/configs/plugins.txt /usr/share/jenkins/ref/
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt

COPY resources/configs/jp.ikedam.jenkins.plugins.extensible_choice_parameter.GlobalTextareaChoiceListProvider.xml /usr/share/jenkins/ref/
COPY resources/configs/org.jenkinsci.plugins.workflow.libs.GlobalLibraries.xml /usr/share/jenkins/ref/
