To setup fully secured jenkins instance you have to replace default self-signed localhost certificates/keystore

1. generate your own keystore.jks based on steps and put into the `./jenkins` folder: https://devopscube.com/configure-ssl-jenkins/

2. Override default Zebrunner Jenkins kestore.jks via docker-compose.yml:
```
  jenkins-master:
    image: "zebrunner/jenkins-master:${TAG_JENKINS_MASTER}"
    container_name: jenkins-master
    env_file:
      - variables.env
    volumes:
     - "data-volume:/var/jenkins_home:rw"
     - "./keystore.jks:/var/jenkins_home/keystore.jks"     
```
3. restart services using `./zebrunner.sh restart`

