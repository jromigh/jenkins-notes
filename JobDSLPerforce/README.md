# Software Requirements
To run this code, you'll need:

1. [Java 1.8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html) -- Ensure **JAVA_HOME** system environment variable is set *before installing Groovy!*
1. [Groovy 2.4+](http://groovy-lang.org/download.html) -- Ensure **GROOVY_HOME** system environment variable is set *before installing Gradle!*
1. [Gradle](https://gradle.org/install/)

I highly recommend installing this software in their DEFAULT installation locations, as I've run into issues where some software assumes install locations and it's a real pain.

# What This Is
This is an example of a Job DSL 'Seeder' (terminology: '[seed jobs/seeders](https://github.com/jenkinsci/job-dsl-plugin/wiki#getting-started)' are Jenkins jobs that run Job DSL files to create other Jenkins jobs) which determines what jobs to create via configuration files and individual Groovy code handling build logic.
If you are only creating a few jobs, this is needlessly complicated! There are far simpler solutions that should suit you.

If you're managing several Perforce streams which a large variety of complex Jenkins jobs, this is one way you can keep code dry. If you're considering looking into my code, I've also included some more notes on how the code works in [FURTHERNOTES.md](FURTHERNOTES.md).

# Workflow
This is how the program works:

1. You run **ExampleSeeder.groovy** (the 'seeder') inside a Jenkins job with a Job DSL build step.
2. The seeder will read configuration data from [ConfigurationFiles/ExampleConfiguration.json](ConfigurationFiles/ExampleConfiguration.json)
3. Using the configuration data, it will know which Perforce streams will need which jobs. The jobs themselves are defined in Groovy files located in [JobTemplates](JobTemplates)
4. The seeder will create a view for each stream.
5. The seeder will execute data from individual Groovy files in [JobTemplates](JobTemplates) creating jobs one at a time for each stream. (It will also substitute any "configuationValues" data defined by [ConfigurationFiles/ExampleConfiguration.json](ConfigurationFiles/ExampleConfiguration.json) if it is called for in the JobTemplate.)

# Assumptions
These examples assumes a few things:
1. You have this code checked into your Perforce server
2. You will be creating jobs programmatically for Mainline ('Main') development, along with other streams (in this example, 'Staging' and 'Dev').
3. The streams will (potentially) need different jobs, so you'll require a system for specifying which streams get which jobs. In this example, we can hold [configuration in a JSON file]().

# Expectations
After we run [runGradleTests.bat](runGradleTests.bat), we should expect the following items in the resulting JobDSLPerforce\\build\\xml folder:

**Jobs**:
* Dev_Test_Job.xml
* Main_Main_Specific_Test_Job.xml
* Main_Test_Job.xml
* Staging_Test_Job.xml

**Views**:
* Dev.xml
* Main.xml
* Staging.xml
