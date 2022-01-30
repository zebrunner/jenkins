# Zebrunner CE - Jenkins Master Image

Feel free to support the development with a [**donation**](https://www.paypal.com/donate?hosted_button_id=JLQ4U468TWQPS) for the next improvements.

<p align="center">
  <a href="https://zebrunner.com/"><img alt="Zebrunner" src="https://github.com/zebrunner/zebrunner/raw/master/docs/img/zebrunner_intro.png"></a>
</p>

## Usage
1. Clone [jenkins-master](https://github.com/zebrunner/jenkins-master) and setup:
   ```
   git clone https://github.com/zebrunner/jenkins-master.git && cd jenkins-master && ./zebrunner.sh setup
   ```
   > Provide valid protocol and hostname
2. Start services `./zebrunner.sh start`
3. Open `http://hostname:8080/jenkins` or `https://hostname:8443/jenkins` for secured protocol
   > Login using admin/changeit credentials
> Follow installation and configuration guide in [Zebrunner CE](https://zebrunner.github.io/community-edition) to reuse Jenkins image effectively for Test Automation.

## Manual deployment steps for 3rd party Jenkins Setup
In order to configure existing Jenkins with automation Pipeline/JobDSL follow the detailed [guide](https://github.com/zebrunner/jenkins-master/blob/master/manual_deployment/README.md)

## Documentation and free support
* [Zebrunner PRO](https://zebrunner.com)
* [Zebrunner CE](https://zebrunner.github.io/community-edition)
* [Zebrunner Reporting](https://zebrunner.com/documentation)
* [Carina Guide](http://zebrunner.github.io/carina)
* [Demo Project](https://github.com/zebrunner/carina-demo)
* [Telegram Channel](https://t.me/zebrunner)
