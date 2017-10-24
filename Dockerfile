FROM jenkins/jenkins:lts-alpine

ENV ROOT_URL=http://localhost:8083/jenkins
ENV ROOT_EMAIL=cloud@qaprosoft.com
ENV ADMIN_USER=admin
ENV ADMIN_PASS=qaprosoft
ENV JENKINS_JOB_DSL_GIT_URL=git@github.com:qaprosoft/jenkins-job-dsl.git
ENV JENKINS_OPTS="--prefix=/jenkins --httpPort=-1 --httpsPort=8083 --httpsKeyStore=/var/jenkins_home/keystore.jks --httpsKeyStorePassword=password"
ENV CARINA_CORE_VERSION=LATEST
ENV CORE_LOG_LEVEL=INFO
ENV DEFAULT_BASE_MAVEN_GOALS="-Dcarina-core_version=\$CARINA_CORE_VERSION -f pom.xml -Dci_run_id=\$ci_run_id -Dcore_log_level=\$CORE_LOG_LEVEL -Demail_list=\$email_list -Dmaven.test.failure.ignore=true -Dselenium_host=\$SELENIUM_HOST -Dmax_screen_history=1 -Dinit_retry_count=0 -Dinit_retry_interval=10 \$ZAFIRA_BASE_CONFIG clean test"
ENV SELENIUM_HOST=http://localhost:4444/wd/hub
ENV ZAFIRA_BASE_CONFIG="-Dzafira_enabled=true -Dzafira_rerun_failures=\$rerun_failures -Dzafira_service_url=\$ZAFIRA_SERVICE_URL -Dgit_branch=\$GIT_BRANCH -Dgit_commit=\$GIT_COMMIT -Dgit_url=\$GIT_URL -Dci_user_id=\$BUILD_USER_ID -Dci_user_first_name=\$BUILD_USER_FIRST_NAME -Dci_user_last_name=\$BUILD_USER_LAST_NAME -Dci_user_email=\$BUILD_USER_EMAIL"
ENV ZAFIRA_SERVICE_URL=https://localhost:8080/zafira-ws

USER root

# Install Git

RUN apk update && apk upgrade && \
    apk add --no-cache bash git openssh


# Install Apache Maven

ARG MAVEN_VERSION=3.5.0
ARG USER_HOME_DIR="/root"
ARG SHA=beb91419245395bd69a4a6edad5ca3ec1a8b64e41457672dc687c173a495f034
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

RUN chown -R jenkins "$USER_HOME_DIR" /usr/share/maven /usr/share/maven/ref /usr/lib/jvm/default-jvm

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
