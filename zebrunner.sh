#!/bin/bash

setup() {
    # PREREQUISITES: valid values inside ZBR_PROTOCOL, ZBR_HOSTNAME and ZBR_PORT env vars!
    local url="$ZBR_PROTOCOL://$ZBR_HOSTNAME:$ZBR_PORT/jenkins"
    
    cp variables.env.original variables.env
    replace variables.env "http://localhost:8080/jenkins" "${url}"
    replace variables.env "INFRA_HOST=localhost:8080" "INFRA_HOST=${ZBR_INFRA_HOST}"
    
    if [[ -n $ZBR_SONAR_URL ]]; then
        replace variables.env "SONAR_URL=" "SONAR_URL=${ZBR_SONAR_URL}"
    fi
    
}

shutdown() {
    if [[ -f .disabled ]]; then
        rm -f .disabled
        exit 0 #no need to proceed as nothing was configured
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
    docker run --rm --volumes-from jenkins-master -v "$(pwd)"/backup:/var/backup "ubuntu" tar -czvf /var/backup/jenkins-master.tar.gz /var/jenkins_home
}

restore() {
    if [[ -f .disabled ]]; then
        exit 0
    fi
    
    stop
    cp variables.env.bak variables.env
    docker run --rm --volumes-from jenkins-master -v "$(pwd)"/backup:/var/backup "ubuntu" bash -c "cd / && tar -xzvf /var/backup/jenkins-master.tar.gz"
    down
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

version() {
    if [[ -f .disabled ]]; then
        exit 0
    fi
    
    # shellcheck disable=SC1091
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
        version        Version of container
    "
    
    echo_telegram
    exit 0
}

BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${BASEDIR}" || exit

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
    version)
        version
    ;;
    *)
        echo "Invalid option detected: $1"
        echo_help
        exit 1
    ;;
esac

