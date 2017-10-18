# Dockerized Jenkins master

### How to run
```
docker run -p 8083:8083 -p 50000:50000 \
          -v ~/jenkins_home:/var/jenkins_home -v ~/.m2:/root/.m2 -v ~/.ssh:/var/jenkins_home/.ssh 
          -e JENKINS_OPTS="--httpPort=-1 --httpsPort=8083 --httpsCertificate=/var/jenkins_home/ssl.crt --httpsPrivateKey=/var/jenkins_home/ssl.key" \
          -d --rm --name jenkins qaprosoft/jenkins-master:latest
```

### Read plugins from remote Jenkins
```
#!/bin/bash

JENKINS_HOST=<username>:<password>@localhost:8080
curl -sSL "http://$JENKINS_HOST/pluginManager/api/xml?depth=1&xpath=/*/*/shortName|/*/*/version&wrapper=plugins" | perl -pe 's/.*?<shortName>([\w-]+).*?<version>([^<]+)()(<\/\w+>)+/\1 \2\n/g'|sed 's/ /:/'
```
