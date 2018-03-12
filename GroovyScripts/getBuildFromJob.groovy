// CONTROL VARIABLES
// Name of the "item" -- usually the job title with any folder(s) the job is in
def itemFullName = "MyBuild"
// Number of the build run. Use 0 for latest, use negative numbers to get number relative to most recent
def buildNumber = 0
// END CONTROL VARIABLES



def item = Jenkins.instance.getItemByFullName(itemFullName)
// Catch if we didn't find a job successfully.
if (item == null) {
  println "Could not find a job with the full name of \"${itemFullName}\"."
  return
}

def run
if (buildNumber == 0) {
  println "\"buildNumber\" is 0, getting latest build..."
  run = item.getLastBuild()
} else {
  if (buildNumber < 0) {
    def nextNum = item.getNextBuildNumber()
    println "Detected negative number, working backwards from ${nextNum}..."
    buildNumber = nextNum - 1 + buildNumber
  }
  println "Getting build #${buildNumber}..."
  run = item.getBuilds().find() { build ->
    build.number == buildNumber
  }
}

// Catch if we didn't find a build successfully.
if (run == null) {
  println "Could not find a run for #${buildNumber}."
  return
}

println "Run: " + run

// From here, you can perform lots of actions on the run! Just
// http://javadoc.jenkins-ci.org/hudson/model/Run.html
