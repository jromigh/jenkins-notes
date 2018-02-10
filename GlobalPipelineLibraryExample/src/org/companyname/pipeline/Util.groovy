package org.companyname.pipeline

import java.util.concurrent.TimeUnit

class Util {
  // A helper function for getLogParserOutputs()
  public static String removeHtml(List inputs) {
    StringBuilder sb = new StringBuilder()
    inputs.each { input ->
      if (input.equals("HEADER HERE: #0")) {
        return
      }

      def string = input.replaceAll("\\<.*?>","")
      string = string.replaceAll("^\\s*(\\d+)\\s*(?:\\^\\d*\\s*)(?:WARNING: )?(?:ERROR: )?", "\$1. ").trim()
      sb.append("\t" + string + "\n")
    }
    return sb.toString()
  }

  // Find the LogParserAction from the build (if any),
  // Then return a string with ERRORS and WARNINGS called out
  // Generally use "currentBuild.rawBuild" from a Pipeline script
  // Otherwise, 'rawBuild' should be hudson.model.Run, or at least inherit from hudson.model.Actionable
  @NonCPS
  public static String getLogParserOutputs(rawBuild) {
    def logParser = rawBuild.getAction(hudson.plugins.logparser.LogParserAction)
    if (logParser == null) {
      return "(No log parser found.)"
    }

    def logParserResult = logParser.getResult()
    def errorLinksFile = logParserResult.getErrorLinksFile()
    def warningLinksFile = logParserResult.getWarningLinksFile()

    StringBuilder emailContent = new StringBuilder()

    if (errorLinksFile != null) {
      def errorLinks = new File(errorLinksFile).readLines()
      emailContent.append("\nERRORS:\n")

      def errorText = removeHtml(errorLinks)
      if(errorText == "") {
        emailContent.append("\tNo errors found.")
      } else {
        emailContent.append(errorText)
      }
    }


    if (warningLinksFile != null) {
      def warningLinks = new File(warningLinksFile).readLines()
      emailContent.append("\nWARNINGS:\n")

      def warningText = removeHtml(warningLinks)
      if(warningText == "") {
        emailContent.append("\tNo warnings found.")
      } else {
        emailContent.append(warningText)
      }
    }

    if (emailContent.toString() == "") {
      return "(Log parser found, but no data retrieved.)"
    }
    return emailContent.toString()
  }

  // Given a label 'labelString' return a list of node names associated
  // with the label
  @NonCPS
  public static HashSet<String> getNodeNamesFromLabel(String labelString) {
    Label label = Jenkins.instance.getLabel(labelString)
    if (label == null) {
      throw new Exception("No labels found with name '" + labelString + "'. Please check the build node configurations.")
    }

    Set<Node> nodes = label.getNodes()
    HashSet<String> nodeNames = new HashSet<String>();
    nodes.each() { node ->
      nodeNames.add(node.getNodeName())
    }

    return nodeNames
  }

  // Utility function for displaying the build duration until this point
  // Generally just use the 'currentBuild' variable from a Pipeline script
  // currentBuild needs to inherit from hudson.model.Run
  public static String getBuildDurationThusFar(currentBuild) {
    long buildStartTime = currentBuild.getStartTimeInMillis()

    return formatTimeDifference(buildStartTime, System.currentTimeMillis())
  }

  // Human readable difference between two times.
  // Mostly a helper function for getBuildDurationThusFar()
  public static String formatTimeDifference(long pastTime, long futureTime) {
    long difference = futureTime - pastTime

    long hours = TimeUnit.MILLISECONDS.toHours(difference)
    long minutes = TimeUnit.MILLISECONDS.toMinutes(difference) % 60
    long seconds = TimeUnit.MILLISECONDS.toSeconds(difference) % 60
    long millis = difference % 1000

    return sprintf("%02d:%02d:%02d.%04d", [hours, minutes, seconds, millis])
  }
}
