# Dockerized Jenkins master

### How to run
```
docker run -p 8080:8080 -p 50000:50000 \
          -v ~/jenkins_home:/var/jenkins_home -v ~/.m2:/root/.m2 \
          -d --rm --name jenkins qaprosoft/jenkins-master:latest
```

### Read plugins from remote Jenkins
```
#!/bin/bash

JENKINS_HOST=<username>:<password>@localhost:8080
curl -sSL "http://$JENKINS_HOST/pluginManager/api/xml?depth=1&xpath=/*/*/shortName|/*/*/version&wrapper=plugins" | perl -pe 's/.*?<shortName>([\w-]+).*?<version>([^<]+)()(<\/\w+>)+/\1 \2\n/g'|sed 's/ /:/'
```
