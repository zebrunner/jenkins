# Zebrunner CE - Jenkins Master Image

Feel free to support the development with a [**donation**](https://www.paypal.com/donate?hosted_button_id=JLQ4U468TWQPS) for the next improvements.

<p align="center">
  <a href="https://zebrunner.com/"><img alt="Zebrunner" src="./docs/img/zebrunner_intro.png"></a>
</p>

## Usage
1. Clone [jenkins-master](https://github.com/zebrunner/jenkins-master) and configure:
   ```
   git clone https://github.com/zebrunner/jenkins-master.git && cd jenkins-master && ./zebrunner.sh setup
   ```
> Provide valid protocol, hostname and port values
2. Start services `./zebrunner.sh start` and open `$ZBR_PROTOCOL://$ZBR_HOSTNAME:$ZBR_PORT/jenkins`
3. Login using admin/changeit credentials
> Follow installation and configuration guide in [Zebrunner CE](https://zebrunner.github.io/zebrunner) to reuse Jenkins image effectively for Test Automation.

## Manual deployment steps for 3rd party Jenkins Setup
In order to configure existing Jenkins with automation Pipeline/JobDSL follow the detailed [guide](https://github.com/zebrunner/jenkins-master/blob/master/manual_deployment/README.md) 
