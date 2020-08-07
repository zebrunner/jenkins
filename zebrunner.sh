#!/bin/bash

  setup() {
    docker network inspect infra >/dev/null 2>&1 || docker network create infra

    if [[ ! -f ${BASEDIR}/variables.env.original ]]; then
      #make a backup of the original file
      cp ${BASEDIR}/variables.env ${BASEDIR}/variables.env.original
    fi

    local url="$ZBR_PROTOCOL://$ZBR_HOSTNAME:$ZBR_PORT/jenkins"
    sed -i "s#http://localhost:8080/jenkins#${url}#g" ${BASEDIR}/variables.env

    if [[ ! -z $ZBR_SONAR_URL ]]; then
      sed -i "s#SONAR_URL=#SONAR_URL=${ZBR_SONAR_URL}#g" ${BASEDIR}/variables.env
    fi

  }

  start() {
    # create infra network only if not exist
    docker network inspect infra >/dev/null 2>&1 || docker network create infra

    if [[ ! -f ${BASEDIR}/.disabled ]]; then
      docker-compose --env-file ${BASEDIR}/.env -f ${BASEDIR}/docker-compose.yml up -d
    fi
  }

  stop() {
    if [[ ! -f ${BASEDIR}/.disabled ]]; then
      docker-compose --env-file ${BASEDIR}/.env -f ${BASEDIR}/docker-compose.yml stop
    fi
  }

  down() {
    if [[ ! -f ${BASEDIR}/.disabled ]]; then
      docker-compose --env-file ${BASEDIR}/.env -f ${BASEDIR}/docker-compose.yml down
    fi
  }

  shutdown() {
    if [[ ! -f ${BASEDIR}/.disabled ]]; then
      docker-compose --env-file ${BASEDIR}/.env -f ${BASEDIR}/docker-compose.yml down -v
    fi

    if [[ -f ${BASEDIR}/variables.env.original ]]; then
      mv ${BASEDIR}/variables.env.original ${BASEDIR}/variables.env
    fi

    echo "TODO: think about backup generation during shutdown."
  }

  backup() {
    echo "TODO: implement logic"
  }

  restore() {
    echo "TODO: implement logic"
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
      For more help join telegram channel https://t.me/qps_infra"
      exit 0
  }


  set_global_settings() {

    # Setup global settings: protocol, hostname and port
    local is_confirmed=0
    ZBR_PROTOCOL=http
    ZBR_HOSTNAME=$HOSTNAME
    ZBR_PORT=80

    while [[ $is_confirmed -eq 0 ]]; do
      read -p "PROTOCOL [$ZBR_PROTOCOL]: " local_protocol
      if [[ ! -z $local_protocol ]]; then
        ZBR_PROTOCOL=$local_protocol
      fi

      read -p "FQDN HOSTNAME [$ZBR_HOSTNAME]: " local_hostname
      if [[ ! -z $local_hostname ]]; then
        ZBR_HOSTNAME=$local_hostname
      fi

      read -p "PORT [$ZBR_PORT]: " local_port
      if [[ ! -z $local_port ]]; then
        ZBR_PORT=$local_port
      fi

      confirm "URL: $ZBR_PROTOCOL://$ZBR_HOSTNAME:$ZBR_PORT" "Continue?"
      is_confirmed=$?
    done

    export ZBR_PROTOCOL=$ZBR_PROTOCOL
    export ZBR_HOSTNAME=$ZBR_HOSTNAME
    export ZBR_PORT=$ZBR_PORT

  }

  confirm() {
    while true; do
      echo "$1"
      read -p "$2 [y/n]" yn
      case $yn in
      [y]*)
        return 1
        ;;
      [n]*)
        return 0
        ;;
      *)
        echo
        echo "Please answer y (yes) or n (no)."
        echo
        ;;
      esac
    done
  }


BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd ${BASEDIR}

case "$1" in
    setup)
        docker network inspect infra >/dev/null 2>&1 || docker network create infra

        if [[ -z $ZBR_PROTOCOL || -z $ZBR_HOSTNAME || -z $ZBR_PORT ]]; then
          set_global_settings
        fi

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
    *)
        echo "Invalid option detected: $1"
        echo_help
        exit 1
        ;;
esac

