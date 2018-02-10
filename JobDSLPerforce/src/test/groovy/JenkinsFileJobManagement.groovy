// https://github.com/unguiculus/job-dsl-sample/blob/master/src/test/groovy/io/unguiculus/jobdsl/JenkinsFileJobManagement.groovy
package io.unguiculus.jobdsl

import hudson.FilePath
import javaposse.jobdsl.dsl.Item
import javaposse.jobdsl.dsl.NameNotProvidedException
import javaposse.jobdsl.plugin.JenkinsJobManagement
import javaposse.jobdsl.plugin.LookupStrategy

// Begin addition by Jake Romigh
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.String.format;
// End addition

import static java.nio.charset.StandardCharsets.UTF_8

class JenkinsFileJobManagement extends JenkinsJobManagement {
    private static final Logger LOGGER = Logger.getLogger(JenkinsFileJobManagement.class.getName());

    private final File xmlDir;

    public JenkinsFileJobManagement(PrintStream outputLogger, Map<String, String> envVars, File workspace, File xmlDir) {
        super(outputLogger, envVars, null, new FilePath(workspace.getAbsoluteFile()), LookupStrategy.JENKINS_ROOT)
        this.xmlDir = xmlDir
    }

    @Override
    public boolean createOrUpdateConfig(Item dslItem, boolean ignoreExisting)
            throws NameNotProvidedException {
        super.createOrUpdateConfig(dslItem, ignoreExisting)

        String path = dslItem.getName()
        File configXml = new File(xmlDir, path + '.xml')
        configXml.getParentFile().mkdirs()

        String xml = dslItem.getXml()
        configXml.setText(dslItem.getXml(), UTF_8.name())

        true
    }

   // Begin addition by Jake Romigh
   @Override
   public void createOrUpdateView(String path, String config, boolean ignoreExisting) {
       LOGGER.log(Level.INFO, format("createOrUpdateView for %s", path));
       super.createOrUpdateView(path, config, ignoreExisting)

       File viewXml = new File(xmlDir, path + '.xml')
       viewXml.getParentFile().mkdirs()
       viewXml.setText(config, UTF_8.name())
   }

}
