import jenkins.model.*
import java.lang.reflect.Field
import org.jenkinsci.plugins.ghprb.*

def descriptor = Jenkins.instance.getDescriptorByType(org.jenkinsci.plugins.ghprb.GhprbTrigger.DescriptorImpl.class)

Field auth = descriptor.class.getDeclaredField("githubAuth")

println "--> setting ghprbhook creds"

auth.setAccessible(true)

githubAuth = new ArrayList<GhprbGitHubAuth>(1)

// serverAPIUrl, jenkinsUrl, credentialsId, description, id, secret
githubAuth.add(new GhprbGitHubAuth("https://api.github.com", "", "ghprbhook-token", "GitHub Pull Request Builder token", "CHANGE_ME", "CHANGE_ME"))

auth.set(descriptor, githubAuth)

descriptor.save()

return
