import jenkins.model.*
import java.lang.reflect.Field
import hudson.util.Secret
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*

println "--> setting generic webhook creds"

def global_domain = Domain.global()

def credentialsStore =
        Jenkins.instance.getExtensionList(
                'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
        )[0].getStore()

def env = System.getenv()

def id = "generic-webhook-token"
def description = "Scm generic webhook token"
def secret = env['GENERIC_WEBHOOK_SECRET']

def genericWebhookCreds = new StringCredentialsImpl(
    CredentialsScope.GLOBAL,
    id,
    description,
    Secret.fromString(secret)
)

credentialsStore.addCredentials(global_domain, genericWebhookCreds)
