import groovy.util.Node

class Common {
  // Simple example of what you can do with a shared library and Job DSL

  // Adds a "log rotator" to trim old builds and save on Jenkins master disk space
  static void addLogRotator(job) {
    // Keep logs for 30 days, but 100 max
    job.logRotator(30, 100, -1, -1)
  }

  // An example of a limitation in a shared library like this -- some of the overridden Node operators don't function properly outside the context.
  // We'll revert to using native groovy.util.Node methods.
  // Add a FilterViewMask to the supplied P4 node
  static void addP4PollingFilters(Node p4ScmNode, String streamName) {
    def viewMaskString = "//depot/${streamName}/ignoreThisPath"

    // We don't have access to the overriden operators '/' and '<<'. Use normal Node methods.
    Node userFilter = new Node(p4ScmNode / 'filter',  "org.jenkinsci.plugins.p4.filters.FilterUserImpl")
    Node userNode = new Node(userFilter, "user", "jenkins")

    Node viewMaskFilter = new Node(p4ScmNode / 'filter',  "org.jenkinsci.plugins.p4.filters.FilterViewMaskImpl")
    Node viewMaskNode = new Node(viewMaskFilter, "viewMask", viewMaskString)
  }
}
