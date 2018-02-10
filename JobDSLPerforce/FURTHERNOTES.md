# Why is ExampleSeeder so complicated?
My goal with this seeder (which is a generalized version of a real seeder I've written) was to extract as much build logic from the seeder as possible. I did not want the seeder to include any business/build logic inside of itself, nor did I want it to be aware of our Perforce code and stream structure. I wanted it to be a simple workhorse who received instructions through configuration files, and created jobs that held build logic for specific tasks. I do this so that the code for the seeder can stay fairly static when our business needs change. We also only modify select job files when build logic needs to change for a select number of jobs -- if all of our various build workflows were included in a massive seeder file, that file would be extremely volatile.

# In-Depth Breakdown

The first thing I want to make clear is this: all of the Job DSL functions you call are only available in the opening context of the Job DSL script. That means, inside the main() functuion I can create jobs and views, but once I'm in a subclass those methods are unavailable. So the first thing we do is create a 'JenkinsHelper' class which holds that context so we can create these jobs inside separate Groovy files.

## JenkinsHelper class
The only reason this class exists is to help us hold on to the opening context of the script. This context allows us to interact with the [Jenkins Job DSL API](https://jenkinsci.github.io/job-dsl-plugin/). We hold the context inside a variable called *dslFactory*, which we also pass along to other script files so they can do the work of creating jobs.

Also:

* Since the process of creating Jenkins views is so simple, this class can handle creating views for each Perforce stream.
* JenkinsHelper will handle reading files from the workspace. It also holds the logic to read files regardless of testing locally on your machine, or on the Jenkins build slaves.
* It can also retrieve environment variable information set by Jenkins during the build step (or local dev)

### JenkinsHelper createJob()
This particular method took me a long time to create and get *just* right -- and I'm not sure it's not actually overengineered. But this is how it works:

1. The JobTemplate we're evaluating is passed in as a giant String -- "jobScriptText". This will eventually be passed to a GroovyShell to evaluate.
1. We creating a 'Binding' object which contains configuration values that we set in the Seeder, along with configuration specified in the JSON file. This will be used for plugging in information to the job groovy file.
1. We include the dslFactory variable in the binding, so the job groovy file can use the [Job DSL API](https://jenkinsci.github.io/job-dsl-plugin/).
1. We create a CompilerConfiguration object which will add our job templates folder to the class path, enabling us to have locally shared libraries (when you have functions you don't need to put in global shared libraries).
1. We create a GroovyShell to evaluate the JobTemplate code, with the custom binding and classpath we have set up previously.

Because we do a lot of heavy lifting in the Seeder setting up these data structures, the actual JobTemplate files can be very straightforward and focus purely on how to generate the specific job it is written for. That should enable the build team to encourage other developers to maintain and contribute to the JobTemplates, giving them more control over how builds function. (The assumption is that the developers should know more about how their products should be built than the build engineers, who are more focused on the tooling. Build engineers can become more of advisor role instead of implementing changes in the builds, and hopefully that will lead to better turnaround times.)

## StreamConfig class
This class holds the configuration information that comes from our JSON file. The class also figures out which jobs to instruct JenkinsHelper to build based on the configuration.

## SeederController class
Simple class that drives the workflow of the seeder. Mostly used for reading the configuration JSON file and calling createAllViewsAndJobs().
