dslFactory.freeStyleJob(STREAM + "_Test_Job") { job ->
  displayName(STREAM + " Test Job")

  // Exampel use of our common library
  Common.addLogRotator(job)

  wrappers {
    timestamps()
  }

  scm {
    perforceP4('CREDENTIAL_ID') {
      configure { node ->
        // Remove old ManualWorkspaceImpl workspace node and replace it with Stream workspace
        node.remove(node / workspace)

        // Add our workspace
        node << workspace(class: 'org.jenkinsci.plugins.p4.workspace.StreamWorkspaceImpl') {
          streamName('//depot/' + STREAM)
          format('jenkins-${NODE_NAME}-${JOB_NAME}')
        }

        Common.addP4PollingFilters(node, STREAM)
      }
    }
  }

  steps {
    batchFile('echo ' + configValue)
  }
}
