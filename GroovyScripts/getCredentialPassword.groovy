// This script requires the use of Credentials Plugin.
// https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin
import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.SystemCredentialsProvider

// CONTROL VARIABLES
// This will be the ID to search for and decrypt
def targetID = "SEARCH_FOR_THIS_ID_AND_GIVE_ME_PLAIN_TEXT_PASSWORDS".trim() // <- We get this value from the credentials page itself
// END CONTROL VARIABLES

// http://javadoc.jenkins.io/credentials/com/cloudbees/plugins/credentials/CredentialsProvider.html
ExtensionList credExtList = Jenkins.getInstance().getExtensionList('com.cloudbees.plugins.credentials.CredentialsProvider')

// Print a list of installed credential providers for reference
println "List of CredentialsProviders installed on this system:\n${credExtList.toString()}\n\n"
/* There are a lot of different credential providers we can look at. Here's an few:
 * com.cloudbees.plugins.credentials.SystemCredentialsProvider
 * com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider
 * com.cloudbees.plugins.credentials.UserCredentialsProvider
 *
 * For this example, let's use SystemCredentialsProvider to look at credentials loaded at the system level.
 */

// http://javadoc.jenkins.io/credentials/com/cloudbees/plugins/credentials/SystemCredentialsProvider.html
println "Retrieving SystemCredentialsProvider..."
ExtensionList systemCredExtList = Jenkins.getInstance().getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')

if (systemCredExtList.size() == 0)  {
  println "ERROR: Could not find the SystemCredentialsProvider."
  return
}

if (systemCredExtList.size() > 1)  {
  println "Ambiguous amounts of SystemCredentialsProviders installed on the system."
  println systemCredExtList
  return
}

// http://javadoc.jenkins.io/credentials/com/cloudbees/plugins/credentials/SystemCredentialsProvider.html
SystemCredentialsProvider systemCredentialsProvider = systemCredExtList.get(0)
List<Credentials> systemCredentials = systemCredentialsProvider.getInstance().getCredentials()

// Iterate through each credential and search for credentialID
println "Searching through system credentials for ID \"${targetID}\""

/* Just like there are a variety of credential providers, there are several types of credentials. Here's a few:
 * com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
 * org.jenkinsci.plugins.docker.commons.credentials.DockerServerCredentials
 * org.jenkinsci.plugins.p4.credentials.P4PasswordImpl
 *
 * Most of these classes extend one of these classes: BaseStandardCredentials, IdCredentials, etc.
 * We should expect a getID() / getId() function at the very least. In this example, we'll also assume there are getUsername() and getPassword() methods.
 * If you are unsure what to use, print out 'systemCredentials' and trace the class hierarchy: http://javadoc.jenkins.io/credentials/com/cloudbees/plugins/credentials/common/package-tree.html
 */

def found = false

systemCredentials.each() { credential ->
  // No need to do further work
  if (found) {
    return
  }

  def credentialID = credential.getId()

  if (credentialID == targetID) {
    found = true
    println "Found credential!\n"
    println sprintf("%-50s %-20s %-20s", ["ID", "Username", "Password"])
    println sprintf("%-50s %-20s %-20s", ["----------", "----------", "----------"])
    // Rememebr -- not all credentials will have getUsername() and getPassword() methods!
    def password = credential.getPassword()

    def passwordPlainText
    if (password instanceof hudson.util.Secret) {
      passwordPlainText = password.getPlainText()
    } else if (password instanceof String) {
      passwordPlainText = hudson.util.Secret.fromString(password)
    } else {
      println "ERROR: Could not understand how to process password's class \"${password.class}\""
      return
    }

    println sprintf("%-50s %-20s %-20s", [credentialID, credential.getUsername(), password])
  }
}

if (!found) {
  println "Could not find credential matching ID \"${targetID}\"."
}

return
