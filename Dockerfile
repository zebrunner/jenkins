FROM jenkins/jenkins:2.135-alpine

ENV ROOT_URL=http://localhost:8083/jenkins
ENV ROOT_EMAIL=qps-auto@qaprosoft.com
ENV ADMIN_EMAILS=qps-auto@qaprosoft.com
ENV ADMIN_USER=admin
ENV ADMIN_PASS=qaprosoft
ENV QPS_HOST=localhost
ENV QPS_PIPELINE_GIT_URL=git@github.com:qaprosoft/qps-pipeline.git
ENV QPS_PIPELINE_GIT_BRANCH=2.6
ENV JENKINS_OPTS="--prefix=/jenkins --httpPort=-1 --httpsPort=8083 --httpsKeyStore=/var/jenkins_home/keystore.jks --httpsKeyStorePassword=password"
ENV ZAFIRA_ACCESS_TOKEN=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwicGFzc3dvcmQiOiJhaTk1Q0JFUmN2MEw4WHZERWozMzV3dkxhK1AxMU50ViIsImV4cCI6MTMwMzYxNjcxMTk2fQ.5S1SA9KP9wXTR9_c-fW9j2fj0e8-3uesDWRv4MfYhrF5O4zSQ2TtzmRpmFjrnroYJ3RTWIf5yUAVJEWTRkKYAw
ENV AWS_KEY=CHANGE_ME
ENV AWS_SECRET=CHANGE_ME

USER root

# Install Git

RUN apk update && apk upgrade && \
    apk add --no-cache bash git openssh


# Install Apache Maven

ARG MAVEN_VERSION=3.5.3
ARG USER_HOME_DIR="/root"
ARG SHA=b52956373fab1dd4277926507ab189fb797b3bc51a2a267a193c931fffad8408
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
