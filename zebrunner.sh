#!/bin/bash

# shellcheck disable=SC1091
source patch/utility.sh

  setup() {
    # load default interactive installer settings
    # shellcheck disable=SC1091
    source backup/settings.env.original

    # load ./backup/settings.env if exist to declare ZBR* vars from previous run!
    if [[ -f backup/settings.env ]]; then
      source backup/settings.env
    fi

    if [[ $ZBR_INSTALLER -eq 1 ]]; then
      # Zebrunner CE installer
      # PREREQUISITES: valid values inside ZBR_PROTOCOL, ZBR_HOSTNAME and ZBR_PORT env vars!
      url="$ZBR_PROTOCOL://$ZBR_HOSTNAME:$ZBR_PORT/jenkins"
      host="$ZBR_HOSTNAME:$ZBR_PORT"
    else
      set_jenkins_settings
      url="$ZBR_PROTOCOL://$ZBR_HOSTNAME:$ZBR_JENKINS_PORT/jenkins"
      host="$ZBR_HOSTNAME:$ZBR_JENKINS_PORT"
    fi

    cp variables.env.original variables.env
    replace variables.env "http://localhost:8080/jenkins" "${url}"
    replace variables.env "INFRA_HOST=localhost:8080" "INFRA_HOST=${host}"

    if [[ ! -z $ZBR_SONAR_URL ]]; then
      replace variables.env "SONAR_URL=" "SONAR_URL=${ZBR_SONAR_URL}"
    fi

    # export all ZBR* variables to save user input
    export_settings
  }

  shutdown() {
    if [[ -f .disabled ]]; then
      rm -f .disabled
      exit 0 #no need to proceed as nothing was configured
    fi

    if [ ! -f variables.env ]; then
      echo_warning "You have to setup services in advance using: ./zebrunner.sh setup"
      echo_telegram
      exit -1
    fi

    docker-compose --env-file .env -f docker-compose.yml down -v
    rm -f variables.env
    rm -f backup/settings.env
  }


  start() {
    if [[ -f .disabled ]]; then
      exit 0
    fi

    if [ ! -f variables.env ]; then
      echo_warning "You have to setup services in advance using: ./zebrunner.sh setup"
      echo_telegram
      exit -1
    fi

    # create infra network only if not exist
    docker network inspect infra >/dev/null 2>&1 || docker network create infra

    if [[ ! -f variables.env ]]; then
      cp variables.env.original variables.env
    fi

    docker-compose --env-file .env -f docker-compose.yml up -d
  }

  stop() {
    if [[ -f .disabled ]]; then
      exit 0
    fi

    if [ ! -f variables.env ]; then
      echo_warning "You have to setup services in advance using: ./zebrunner.sh setup"
      echo_telegram
      exit -1
    fi

    docker-compose --env-file .env -f docker-compose.yml stop
  }

  down() {
    if [[ -f .disabled ]]; then
      exit 0
    fi

    if [ ! -f variables.env ]; then
      echo_warning "You have to setup services in advance using: ./zebrunner.sh setup"
      echo_telegram
      exit -1
    fi

    docker-compose --env-file .env -f docker-compose.yml down
  }

  backup() {
    if [[ -f .disabled ]]; then
      exit 0
    fi

    if [ ! -f variables.env ]; then
      echo_warning "You have to setup services in advance using: ./zebrunner.sh setup"
      echo_telegram
      exit -1
    fi

    cp variables.env variables.env.bak
    docker run --rm --volumes-from jenkins-master -v "$(pwd)"/backup:/var/backup "ubuntu" tar -czvf /var/backup/jenkins-master.tar.gz /var/jenkins_home
  }

  restore() {
    if [[ -f .disabled ]]; then
      exit 0
    fi

    if [ ! -f variables.env ]; then
      echo_warning "You have to setup services in advance using: ./zebrunner.sh setup"
      echo_telegram
      exit -1
    fi

    stop
    cp variables.env.bak variables.env
    docker run --rm --volumes-from jenkins-master -v "$(pwd)"/backup:/var/backup "ubuntu" bash -c "cd / && tar -xzvf /var/backup/jenkins-master.tar.gz"
    down
  }

  version() {
    if [[ -f .disabled ]]; then
      exit 0
    fi

    source .env
    echo "jenkins-master: ${TAG_JENKINS_MASTER}"
  }

  echo_warning() {
    echo "
      WARNING! $1"
  }

  echo_telegram() {
    echo "
      For more help join telegram channel: https://t.me/zebrunner
      "
  }

  set_jenkins_settings() {
    # Setup global settings: protocol, hostname and port
    echo "Zebrunner Jenkins General Settings"
    local is_confirmed=0
    if [[ -z $ZBR_HOSTNAME ]]; then
      ZBR_HOSTNAME=`curl -s ifconfig.me`
    fi

    while [[ $is_confirmed -eq 0 ]]; do
      read -r -p "Protocol [$ZBR_PROTOCOL]: " local_protocol
      if [[ ! -z $local_protocol ]]; then
        ZBR_PROTOCOL=$local_protocol
      fi

      read -r -p "Fully qualified domain name (ip) [$ZBR_HOSTNAME]: " local_hostname
      if [[ ! -z $local_hostname ]]; then
        ZBR_HOSTNAME=$local_hostname
      fi

      if [[ "$ZBR_PROTOCOL" == "http" ]]; then
          ZBR_JENKINS_PORT=8080
      else
          ZBR_JENKINS_PORT=8443
      fi

      confirm "Zebrunner Jenkins URL: $ZBR_PROTOCOL://$ZBR_HOSTNAME:$ZBR_JENKINS_PORT/jenkins" "Continue?" "y"
      is_confirmed=$?
    done

    export ZBR_PROTOCOL=$ZBR_PROTOCOL
    export ZBR_HOSTNAME=$ZBR_HOSTNAME
    export ZBR_JENKINS_PORT=$ZBR_JENKINS_PORT

  }

  echo_help() {
    echo "
      Usage: ./zebrunner.sh [option]
      Flags:
          --help | -h    Print help
      Arguments:
          start          Start container
          stop           Stop and keep container
          restart        Restart container
          down           Stop and remove container
          shutdown       Stop and remove container, clear volumes
          backup         Backup container
          restore        Restore container
          version        Version of container"
      echo_telegram
      exit 0
  }

  replace() {
    #TODO: https://github.com/zebrunner/zebrunner/issues/328 organize debug logging for setup/replace
    file=$1
    #echo "file: $file"
    content=$(<"$file") # read the file's content into
    #echo "content: $content"

    old=$2
    #echo "old: $old"

    new=$3
    #echo "new: $new"
    content=${content//"$old"/$new}

    #echo "content: $content"
    printf '%s' "$content" >"$file"    # write new content to disk
  }


BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${BASEDIR}" || exit

case "$1" in
    setup)
          setup
        ;;
    start)
	start
        ;;
    stop)
        stop
        ;;
    restart)
        down
        start
        ;;
    down)
        down
        ;;
    shutdown)
        shutdown
        ;;
    backup)
        backup
        ;;
    restore)
        restore
        ;;
    version)
        version
        ;;
    *)
        echo "Invalid option detected: $1"
        echo_help
        exit 1
        ;;
esac

