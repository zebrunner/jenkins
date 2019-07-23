FROM ubuntu:16.04

LABEL maintainer "Alex Khursevich <alex@qaprosoft.com>"

ENV DEBIAN_FRONTEND=noninteractive

#=============
# Set WORKDIR
#=============
WORKDIR /root

#==================
# General Packages
#==================
RUN apt-get -qqy update && \
    apt-get -qqy --no-install-recommends install \
    openjdk-8-jdk \
    ca-certificates \
    tzdata \
    unzip \
    curl \
    wget \
    libqt5webkit5 \
    libgconf-2-4 \
    xvfb \
    socat \
    git \
    openssh-server \
  && rm -rf /var/lib/apt/lists/*

#===============
# Install Maven 3.5.2
#===============
RUN cd /opt && \
    wget https://archive.apache.org/dist/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.zip && \
    unzip apache-maven-3.5.2-bin.zip && \
    rm apache-maven-3.5.2-bin.zip && \
    mv apache-maven-3.5.2/ maven/

#===============
# Set JAVA_HOME and M2_HOME
#===============
ENV JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/jre" \
    M2_HOME="/opt/maven" \
    MAVEN_HOME="/opt/maven"
ENV PATH=$PATH:$JAVA_HOME/bin:$M2_HOME/bin

#=====================
# Install Android SDK
#=====================
ARG SDK_VERSION=sdk-tools-linux-3859397
ARG ANDROID_BUILD_TOOLS_VERSION=26.0.0
ENV SDK_VERSION=$SDK_VERSION \
    ANDROID_BUILD_TOOLS_VERSION=$ANDROID_BUILD_TOOLS_VERSION \
    ANDROID_HOME=/root

RUN wget -O tools.zip https://dl.google.com/android/repository/${SDK_VERSION}.zip && \
    unzip tools.zip && rm tools.zip && \
    chmod a+x -R $ANDROID_HOME && \
    chown -R root:root $ANDROID_HOME
ENV PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin

# https://askubuntu.com/questions/885658/android-sdk-repositories-cfg-could-not-be-loaded
RUN mkdir -p ~/.android
RUN touch ~/.android/repositories.cfg

RUN echo y | sdkmanager "platform-tools"
RUN echo y | sdkmanager "build-tools;$ANDROID_BUILD_TOOLS_VERSION"
ENV PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/$ANDROID_BUILD_TOOLS_VERSION

ADD adbkey /root/.android/adbkey
ADD adbkey.pub /root/.android/adbkey.pub

#======================
# Install Jenkins swarm
#======================
ENV JENKINS_SLAVE_ROOT="/opt/jenkins"

USER root

RUN mkdir -p "$JENKINS_SLAVE_ROOT"
RUN mkdir -p /opt/apk

# Slave settings
ENV JENKINS_MASTER_USERNAME="jenkins" \
    JENKINS_MASTER_PASSWORD="jenkins" \
    JENKINS_MASTER_URL="http://jenkins:8080/" \
    JENKINS_SLAVE_MODE="exclusive" \
    JENKINS_SLAVE_NAME="swarm-$RANDOM" \
    JENKINS_SLAVE_WORKERS="1" \
    JENKINS_SLAVE_LABELS="" \
    AVD=""

RUN apt-get clean && apt-get update && apt-get install -y locales
RUN locale-gen en_US.UTF-8 
ENV LANG en_US.UTF-8 
ENV LANGUAGE en_US:en 
ENV LC_ALL en_US.UTF-8

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

# Install Jenkins slave (swarm)
ADD swarm.jar /
ADD entrypoint.sh /


ENTRYPOINT /entrypoint.sh

