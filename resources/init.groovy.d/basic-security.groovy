import jenkins.model.*
import hudson.model.*;
import hudson.security.*

def instance = Jenkins.getInstance()
def env = System.getenv()
def user = env['ADMIN_USER']
def pass = env['ADMIN_PASS']

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount(user, pass)
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
instance.setAuthorizationStrategy(strategy)
instance.save()
