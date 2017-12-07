import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*

println "--> setting aws creds"

def global_domain = Domain.global()

def credentialsStore =
  Jenkins.instance.getExtensionList(
    'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
  )[0].getStore()

def env = System.getenv()
def id = "aws-jacoco-token"
def username = env["AWS_KEY"]
def password = env["AWS_SECRET"]
def description = "AWS S3 token"

def credentials = new UsernamePasswordCredentialsImpl(
  CredentialsScope.GLOBAL,
  id,
  description,
  username,
  password
)

credentialsStore.addCredentials(global_domain, credentials)
