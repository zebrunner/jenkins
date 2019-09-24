import hudson.*
import hudson.security.*
import jenkins.model.*
 
def instance = Jenkins.getInstance()
 
println "Checking if security has been set already"
 
if (!instance.isUseSecurity()) {
    println "creating local user 'admin'"
 
    // with new HudsonPrivateSecurityRealm(true) self registration for new Users is enabled
    def hudsonRealm = new HudsonPrivateSecurityRealm(false)
    hudsonRealm.createAccount('{{ jenkins_admin_username }}', '{{ jenkins_admin_password }}')
    instance.setSecurityRealm(hudsonRealm)
 
    def strategy = new ProjectMatrixAuthorizationStrategy()
    strategy.add(instance.ADMINISTER, "{{ jenkins_admin_username }}")
    // enables global read to anonymous
    strategy.add(Permission.READ, instance.ANONYMOUS.getName())
    // activates read permission to complete Jenkins instance to authenticated users
    strategy.add(instance.READ, "authenticated")
    instance.setAuthorizationStrategy(strategy)
    instance.save()
}
