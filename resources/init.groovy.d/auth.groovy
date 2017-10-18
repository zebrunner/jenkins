import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstance()

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount(env['ADMIN_NAME'], env['ADMIN_PASS'])
instance.setSecurityRealm(hudsonRealm)

def strategy = new GlobalMatrixAuthorizationStrategy()
strategy.add(Jenkins.ADMINISTER, env['ADMIN_NAME'])
instance.setAuthorizationStrategy(strategy)

instance.save()
