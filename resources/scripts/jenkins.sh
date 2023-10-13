#! /bin/bash -e

# Check if the plugins dir is empty and if so install plugins
echo "Check if plugins dir is empty:"
if [ ! "$(ls -A /var/jenkins_home/plugins)" ]; then
  echo "- plugins dir is empty, installing plugins"
  echo "plugins to install: "
  echo "$( cat /var/jenkins_home/plugins.txt )"
  /bin/jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt
else
  echo "- plugins dir is not empty, no actions needed"
  echo "installed plugins:"
  echo "$(ls -d /var/jenkins_home/plugins/*/)"
fi

# upload extra plugins at run-time during first initialization only
if [[ -f /var/jenkins_home/extra_plugins.txt.installed ]]; then
  echo "Zebrunner Jenkins extra-plugins already installed."
else
  if [[ -f /usr/share/jenkins/ref/extra_plugins.txt ]] ; then
    echo "Installing plugins from extra_plugins.txt..."
    /bin/jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/extra_plugins.txt
    cp /usr/share/jenkins/ref/extra_plugins.txt /var/jenkins_home/extra_plugins.txt.installed
  fi
fi

: "${JENKINS_WAR:="/usr/share/jenkins/jenkins.war"}"
: "${JENKINS_HOME:="/var/jenkins_home"}"
: "${COPY_REFERENCE_FILE_LOG:="${JENKINS_HOME}/copy_reference_file.log"}"
: "${REF:="/usr/share/jenkins/ref"}"
touch "${COPY_REFERENCE_FILE_LOG}" || { echo "Can not write to ${COPY_REFERENCE_FILE_LOG}. Wrong volume permissions?"; exit 1; }
echo "--- Copying files at $(date)" >> "$COPY_REFERENCE_FILE_LOG"
find "${REF}" \( -type f -o -type l \) -exec bash -c '. /usr/local/bin/jenkins-support; for arg; do copy_reference_file "$arg"; done' _ {} +

# if `docker run` first argument start with `--` the user is passing jenkins launcher arguments
if [[ $# -lt 1 ]] || [[ "$1" == "--"* ]]; then

  # read JAVA_OPTS and JENKINS_OPTS into arrays to avoid need for eval (and associated vulnerabilities)
  java_opts_array=()
  while IFS= read -r -d '' item; do
    java_opts_array+=( "$item" )
  done < <([[ $JAVA_OPTS ]] && xargs printf '%s\0' <<<"$JAVA_OPTS")

  readonly agent_port_property='jenkins.model.Jenkins.slaveAgentPort'
  if [ -n "${JENKINS_SLAVE_AGENT_PORT:-}" ] && [[ "${JAVA_OPTS:-}" != *"${agent_port_property}"* ]]; then
    java_opts_array+=( "-D${agent_port_property}=${JENKINS_SLAVE_AGENT_PORT}" )
  fi

  if [[ "$DEBUG" ]] ; then
    java_opts_array+=( \
      '-Xdebug' \
      '-Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=y' \
    )
  fi

  jenkins_opts_array=( )
  while IFS= read -r -d '' item; do
    jenkins_opts_array+=( "$item" )
  done < <([[ $JENKINS_OPTS ]] && xargs printf '%s\0' <<<"$JENKINS_OPTS")

  exec java -Duser.home="$JENKINS_HOME" "${java_opts_array[@]}" -jar ${JENKINS_WAR} "${jenkins_opts_array[@]}" "$@"
fi

# As argument is not jenkins, assume user want to run his own process, for example a `bash` shell to explore this image
exec "$@"
