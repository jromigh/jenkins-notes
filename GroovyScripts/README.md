# Note

If you find yourself using a Groovy script often, consider using the [Scriptler Plugin](https://wiki.jenkins.io/display/JENKINS/Scriptler+Plugin). You might not want to use Scriptler with any script that returns sensitive material (like passwords) or performs dangerous activity (modify job data or running jobs).

# List of Groovy Scripts

## System Groovy Scripts
* [cancelQueueItems.groovy](cancelQueueItems.groovy) - Script that cancels all items currently sitting in the queue.
* [getBuildFromJob.groovy](getBuildFromJob.groovy) - Script that gets a specific build (Run) from a job in Jenkins
* [getCredentialPassword.groovy](getCredentialPassword.groovy) - Script that extracts a plain-text password from a credential from the [Credentials Plugin](https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin).
* [listInstalledPlugins.groovy](listInstalledPlugins.groovy) - Script that lists all installed plugins and which have an update available.
