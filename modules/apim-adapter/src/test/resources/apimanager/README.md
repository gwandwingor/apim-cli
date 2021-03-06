
# Purpose
The project is using Integration-Tests to simulate tasks the API-Developer is doing. That means, creating the API for the first time, doing frequent changes, subscribe with applications, etc. 
For that TravisCI is using, which is starting an API-Manager V7.x Docker-Container to perform these integration tests. This document describes the steps needed to build the Docker-Image.

## Steps
Download the API-Gateway/API-Manager release from support.axway.com you want to test with and have the following ready:
- APIGateway_x.x.x_xxx_Install_linux-x86-64_BNxxxxxx.run
- APIGateway_x.x.x-x_ScriptsPackageDocker_linux-x86-64_BNxxxxxxx.tar.gz
- a license without hostname binding
Copy everything to a system having docker installed. 
Perform the following steps:
```
    cd $HOME
    git clone https://github.com/Axway-API-Management-Plus/apim-cli.git
    mkdir apim-cli-dockerimage
    cp APIGateway_x.x.x_xxx_Install_linux-x86-64_BNxxxxxx.run apim-cli-dockerimage
    cp APIGateway_7.7.20200130-1_DockerScripts.tar.gz apim-cli-dockerimage
    cp multiple.lic apim-cli-dockerimage
    $HOME/apim-cli/modules/apim-adapter/src/test/resources/apimanager/buildDockerImage.sh 7.7-20200930
```

### Added Untrusted Docker-Registry
```
vi /etc/docker/daemon.json
```
Add the following
```json
{
  "insecure-registries" : ["docker-registry.demo.axway.com"]
}
systemctl restart dockerd
```

Please note, that you need Write-Permissions to the Docker-Repository to push the image!

To enable the image, please adjust the image referred in the Travis-CI configuration file:
```
edit .travis.yml
and change:
Add/register your new Docker-Image and reference it in the following environment variables:
- DOCKER_IMAGE_TO_USE=$APIM_DOCKER_IMAGE_7_6_2
- CACHE_FILE_TO_USE=$CACHE_FILE_APIM_7_6_2
```
After checkin & commit a Travis-CI build is started using the provided Docker-Image.
