// https://github.com/unguiculus/job-dsl-sample/blob/master/src/test/groovy/io/unguiculus/jobdsl/JobScriptsSpec.groovy
package io.unguiculus.jobdsl

import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement
import org.junit.ClassRule
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class JobScriptsSpec extends Specification {
    @Shared
    @ClassRule
    JenkinsRule jenkinsRule = new JenkinsRule()

    // Begin modification by Jake Romigh
    // During the seeder, we use some environment variables the Jenkins seeder would set for us (if this were a real run)
    // For the Gradle test, we need to recreate these manually.
    Map<String, String> envVars = [
      BUILD_URL: 'FAKE_WEB_ADDR_SeeJobScriptSpec',
      JOB_BASE_NAME: 'FAKE_BASE_NAME_SeeJobScriptSpec',
      WORKSPACE: System.getProperty("user.dir") // We just need the root of execution.
    ]
    // End modification

    @Unroll
    def 'test script #file.name'(File file) {
        given:
        // Begin modification by Jake Romigh
        // We add the previously made envVars map to the JenkinsFileJobManagement constructor
        def jobManagement = new JenkinsFileJobManagement(System.out, envVars, new File('.'), , new File('build', 'xml'))
        // End modification

        when:
        new DslScriptLoader(jobManagement).runScript(file.text)

        then:
        noExceptionThrown()

        where:
        file << jobFiles
    }

    static List<File> getJobFiles() {
        List<File> files = []
        new File('jobs').eachFileRecurse {
            // Begin modification by Jake Romigh
            // Only process explicit Seeder files
            if (it.name.endsWith('Seeder.groovy')) {
                files << it
            }
            // End modification
        }
        files
    }
}
