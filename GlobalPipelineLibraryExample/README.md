# Setting up Jenkins System Configuration
The official documentation is extremely useful here: (https://jenkins.io/doc/book/pipeline/shared-libraries/)
I found the explanation for a 'Legacy' SCM lacking for Perforce, so I included a screenshot here.

Your configuration should look something like this:
![Jenkins Configuration for Global Pipeline Libraries](https://raw.githubusercontent.com/jromigh/jenkins-notes/master/GlobalPipelineLibraryExample/img/PipelineLibrariesConfiguration.png)

# Takeaways

1. We pin at '${library.global-pipeline-utilities.version}' in order to allow different versions of the libraries to be used
2. Workspace name doesn't seem to matter as long as you match it in the view mapping
3. It is very important that your libraries have a 'src' folder structure containing your Groovy code, as that is [where Jenkins will expect to find it](https://jenkins.io/doc/book/pipeline/shared-libraries/#directory-structure) (if not in a /var folder).S

# How to Invoke
Once you have this set up, it becomes fairly trivial to invoke. Example JenkinsFile:

```groovy
@Library('global-pipeline-utilities')
import org.companyname.pipeline.Util

stage('Test Pipeline Library Duration') {
    echo "Built-in Duration = " + currentBuild.duration
    echo "Pipeline Library Duration = " + Util.getBuildDurationThusFar(currentBuild)
}

stage('Test Log Parser Data Retrieval') {
    node('Windows') {
        step([$class: 'LogParserPublisher', useProjectRule: false])
    }
    echo "Log Parser: " + Util.getLogParserOutputs(currentBuild.rawBuild)
}
```
