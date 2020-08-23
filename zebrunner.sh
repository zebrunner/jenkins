#!/bin/bash

  setup() {
    # PREREQUISITES: valid values inside ZBR_PROTOCOL, ZBR_HOSTNAME and ZBR_PORT env vars!
    local url="$ZBR_PROTOCOL://$ZBR_HOSTNAME:$ZBR_PORT/jenkins"

    cp variables.env.original variables.env
    sed -i "s#http://localhost:8080/jenkins#${url}#g" variables.env
    sed -i "s#INFRA_HOST=localhost:8080#INFRA_HOST=${ZBR_INFRA_HOST}#g" variables.env

    if [[ ! -z $ZBR_SONAR_URL ]]; then
      sed -i "s#SONAR_URL=#SONAR_URL=${ZBR_SONAR_URL}#g" variables.env
    fi

  }

  shutdown() {
    if [[ -f .disabled ]]; then
      exit 0
    fi

    docker-compose --env-file .env -f docker-compose.yml down -v
    rm -f variables.env
  }


  start() {
    if [[ -f .disabled ]]; then
      exit 0
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

    docker-compose --env-file .env -f docker-compose.yml stop
  }

  down() {
    if [[ -f .disabled ]]; then
      exit 0
    fi

    docker-compose --env-file .env -f docker-compose.yml down
  }

  backup() {
    if [[ -f .disabled ]]; then
      exit 0
    fi

    cp variables.env variables.env.bak
    docker run --rm --volumes-from jenkins-master -v $(pwd)/backup:/var/backup "ubuntu" tar -czvf /var/backup/jenkins-master.tar.gz /var/jenkins_home
  }

  restore() {
    if [[ -f .disabled ]]; then
      exit 0
    fi

    stop
    cp variables.env.bak variables.env
    docker run --rm --volumes-from jenkins-master -v $(pwd)/backup:/var/backup "ubuntu" bash -c "cd / && tar -xzvf /var/backup/jenkins-master.tar.gz"
    down
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
          restore        Restore container"
      echo_telegram
      exit 0
  }

BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd ${BASEDIR}

case "$1" in
    setup)
        if [[ $ZBR_INSTALLER -eq 1 ]]; then
          setup
        else
          echo_warning "Setup procedure is supported only as part of Zebrunner Server (Community Edition)!"
          echo_telegram
        fi
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
    *)
        echo "Invalid option detected: $1"
        echo_help
        exit 1
        ;;
esac

