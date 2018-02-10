dslFactory.freeStyleJob(STREAM + "_Main_Specific_Test_Job") { job ->
  displayName(STREAM + " Main Specific Test Job")

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
    batchFile('echo This is the Main Specific Job! Maybe this is a release or deploy job only Main should do.')
    batchFile('echo ' + configValue)
  }
}
