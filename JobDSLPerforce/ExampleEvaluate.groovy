// You can either load this file in an interactive Groovy terminal, or copy and paste this code from the JobDSLPerforce folder context into groovysh or GroovyConsole.
// This should allow you to play around with the data structures in the ExampleSeeder, and get a better understanding of how it all comes together.

GroovyShell shell = new GroovyShell()
def seederShell = shell.parse(new File('jobs/ExampleSeeder.groovy'))

seederShell.setupDebug()

//println seederShell.exSeeder
