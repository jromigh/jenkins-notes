import groovy.json.JsonSlurper
import java.util.regex.*

import org.codehaus.groovy.control.CompilerConfiguration

// Misc methods to help the Groovy script run in Jenkins the way it would locally.
// This class also handles calling the actual Job DSL API.
class JenkinsHelper {
  def dslFactory

  JenkinsHelper(dslFactoryContext) {
    this.dslFactory = dslFactoryContext
  }

  String getJenkinsBuildEnvVariable(variableName) {
    try {
      return dslFactory.evaluate("\"\${" + variableName + "}\"")
    }
    catch (MissingPropertyException mpe) {
      println "WARNING: No such Jenkins build environment property " + variableName + " found. Please check your configuration."
      return null
    }
  }

  // Jenkins Job DSL reading files from the workspace is stupid and it should feel bad.
  // This method handles the complexity of determining if we are running in a Jenkins environment, or locally for testing.
  String readFile(String filePath) {
    def fileText = null

    // First try the Jenkins Job DSL method.
    try {
      fileText = dslFactory.readFileFromWorkspace(filePath)
    }
    catch (MissingMethodException mme) {
      println "Recieved MissingMethodException when calling readFileFromWorkspace(). Assuming non-Jenkins environment."
      //println mme
    }

    // If fileText is still null, we were not able to read it. Assume this isn't Jenkins, and try to open the file the 'normal' way
    if (fileText == null) {
      def file = new File(filePath)
      try {
        // Simple tests on the file passed in to ensure it is proper
        assert file.exists() : "File " + filePath + " not found"
        assert file.isFile() : "File path " + filePath + " is not a file"
        assert file.canRead() : "File " + filePath + " cannot be read"

        fileText = file.getText()
      }
      catch (AssertionError a) {
        println "Error encountered processing \"" + filePath + "\": " + a.getMessage()
        throw a
      }
    }

    return fileText
  }

  def createView(streamName) {
    dslFactory.listView(streamName) {
      description("Repository of jobs generated from the " + streamName + " stream.\n" +
        "This view is automatically generated from the ${getJenkinsBuildEnvVariable("JOB_BASE_NAME")} seeder job.\n" +
        "(${getJenkinsBuildEnvVariable("BUILD_URL")})")
      columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
      }

      // If this proves to be too far reaching, we can actually use the object returned from the listView call and add jobs after the fact
      jobFilters {
        regex {
          regex('.*' + streamName + '.*')
        }
        // Excluse "Server Test" jobs
        regex {
          matchType(MatchType.EXCLUDE_MATCHED)
          regex('ST.*')
        }
      }
    }
  }

  // To create a job in Jenkins, we'll need our context from the main() entry point (in 'dslFactory'). We'll pass that along to the 'JobSeeders' Groovy files
  // via the 'dslFactory' variable in the job configuration binding. The GroovyShell class will then execute the Groovy file, which is currently a giant
  // String object passed in as jobScriptText.
  def createJob(jobScriptText, jobConfigurationMap) {
    Binding jobBinding = new Binding(jobConfigurationMap)
    jobBinding.setVariable("dslFactory", dslFactory)

    // This allows us to use the 'Common' class and keep code 'DRY'
    // http://mrhaki.blogspot.com/2011/06/groovy-goodness-add-imports.html
    CompilerConfiguration cc = new CompilerConfiguration()
    cc.setClasspath([getJenkinsBuildEnvVariable("WORKSPACE"), "jobs", "JobTemplates"].join(File.separator))

    GroovyShell shell = new GroovyShell(jobBinding, cc)
    shell.evaluate(jobScriptText)
  }

  // Jenkins output is stupid and it should feel bad.
  // We have to use the entry script context to print, otherwise classes can't output ANYTHING
  // See the variable 'out' which Jenkins automatically sets in the initial context
  void println(val) {
    dslFactory.println(val)
  }

  void print(val) {
    dslFactory.print(val)
  }
}

// Class that holds stream configuration and included jobs
class StreamConfig {
  JenkinsHelper jenkinsHelper
  def stream = ""
  def configurationMap = [:]
  def jobs = []

  StreamConfig(JenkinsHelper jenkinsHelp, String streamName, Map genericConfiguration, Map streamSpecificConfiguration) {
    this.jenkinsHelper = jenkinsHelp
    this.stream = streamName

    // Specifically set up this common configuration value
    this.configurationMap["STREAM"] = this.stream

    // This has the bonus effect of overriding any generic configuration, allowing us more control
    this.configurationMap += genericConfiguration.configuationValues + streamSpecificConfiguration.configuationValues
    this.jobs = genericConfiguration.jobs + streamSpecificConfiguration.jobs

    // Have a variable where we can check our stream's configured jobs, useful for controlling downstream job logic
    this.configurationMap["STREAM_JOBS"] = this.jobs
  }

  // Pretty print our stream configuration map. Use tabs to make it very easy to read.
  String toString() {
    def ret = "Stream: " + stream + "\n\tConfig Values:"
    configurationMap.each() { key,value ->
      ret += "\n\t\t" + key + " = " + value
    }

    ret += "\n"

    if(jobs.isEmpty()) {
      ret += "\tJobs:\n\t\tNo jobs found."
    } else {
      ret += "\tJobs:\n\t\t" + jobs.join("\n\t\t")
    }

    return ret
  }

  // Create a top-level view in Jenkins so users can quickly navigate (or bookmark) a small list of jobs filtered by stream that they're concerned with.
  void createStreamView() {
    jenkinsHelper.createView(stream)
  }


  // Using the Groovy files inside the JobSeeders folder, create the jobs listed inside our configuration map
  void createStreamJobs() {
    jobs.each() { job ->
      def jobFilePath = "jobs\\JobTemplates\\" + job + ".groovy"
      jenkinsHelper.println stream + ": Creating job \"" + job + "\" with \"" + jobFilePath +"\""

      def jobScriptText = jenkinsHelper.readFile(jobFilePath)
      jenkinsHelper.createJob(jobScriptText, configurationMap)
    }
  }
}

// Class that reads and organizes configuration from files on disk
class SeederController {
  JenkinsHelper jenkinsHelper
  Map<String, StreamConfig> streamMap

  SeederController(JenkinsHelper jenkinsHelp) {
    this.jenkinsHelper = jenkinsHelp

    this.streamMap = processStreamInformation("ConfigurationFiles\\ExampleConfiguration.json")
  }

  // Reads text from a file, parses it in JSON and returns the JSON object
  def readJsonFromFile(String filePath) {
    def fileText = jenkinsHelper.readFile(filePath)
    def jsonSlurper = new JsonSlurper()

    def jsonObj = null

    try {
      jsonObj = jsonSlurper.parseText(fileText)
    }
    catch (Exception e) {
      jenkinsHelper.println "Exception encountered reading JSON from \"" + filePath + "\": " + e.getMessage()
      throw e
    }

    return jsonObj
  }

  // Create stream map of StreamName : StreamConfig object
  Map<String, StreamConfig> processStreamInformation(String configFilePath) {
    def configurationJson = readJsonFromFile(configFilePath)

    // Create Seeders for each Stream
    Map<String, StreamConfig> streamMapRet = [:]
    configurationJson.streams.each { streamName, streamSpecificConfig ->
      // Skip the "All" section, which every stream will use
      if (streamName == "All") {
        return
      }

      jenkinsHelper.println "Processing " + streamName + "..."
      streamMapRet[streamName] = new StreamConfig(jenkinsHelper, streamName, configurationJson.streams.All, streamSpecificConfig)
    }

    return streamMapRet
  }

  // Simply pretty print our configuration map
  String toString() {
    def string = "\nStream Map:\n"

    streamMap.each { _, streamConfig ->
      string += streamConfig.toString() + "\n"
    }

    return string
  }

  def createAllViewsAndJobs() {
    jenkinsHelper.println "Creating all stream jobs.\n"
    streamMap.each() { streamName, streamConfig ->
      jenkinsHelper.println "Creating view for " + streamName + "..."
      streamConfig.createStreamView()

      jenkinsHelper.println "Creating jobs for " + streamName + ":"
      streamConfig.createStreamJobs()
      jenkinsHelper.println "\n"
    }

    jenkinsHelper.println "Stream view and job generation complete."
  }
}

public static main(String... args) {
  // We need to pass in the "dslFactory" context so we have access to the Jenkins Job DSL
  // https://github.com/jenkinsci/job-dsl-plugin/wiki/Job-DSL-Commands#dsl-factory
  JenkinsHelper jenkinsHelper = new JenkinsHelper(this)

  def exSeeder = new SeederController(jenkinsHelper)
  jenkinsHelper.println exSeeder
  exSeeder.createAllViewsAndJobs()
}

// This is a function we call during local testing.
def setupDebug() {
  // This is an example of how we can retrieve Jenkins env variables set via Job DSL
  // Groovyfu - Without defs in front of the variable names, they become globally available and thus mock environment variables
  testJenkinsEnvVar = "Hello World!"
  JOB_BASE_NAME = "FAKE_JOB_BASE_NAME_SeeSeederDebugMethod"
  BUILD_URL = "http://FAKE_WEB_ADDRESS_SeeSeederDebugMethod"

  JenkinsHelper jenkinsHelper = new JenkinsHelper(this)

  jenkinsHelper.println "Setting up \"exSeeder\" variable to hold configuration..."
  // Don't include a def so the debugging script can reference this variable
  exSeeder = new SeederController(jenkinsHelper)
  jenkinsHelper.println exSeeder
  jenkinsHelper.println "\nReady.\n"
}
