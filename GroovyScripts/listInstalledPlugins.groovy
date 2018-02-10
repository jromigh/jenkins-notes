// If you need to determine which plugins you're Jenkins instance requires, use this workflow:
//
// 1. Start your existing Jenkins instance
// 2. Use the Plugin Manager in "Configure Jenkins" to install needed plugins and resolve dependencies
// 3. Run this script in the System Groovy Console with the 'Groovy' plugin
// 4. Replace the plugins.txt file contexts with the resulting output

def format(String str1, String str2, String str3) {
  return sprintf("%-50s %-20s %16s", [str1, str2, str3])
}

def formatPluginEntry(String str1, String str2, boolean hasUpdate) {
  if (hasUpdate) {
    return format(str1, str2, "")
  } else {
    return format(str1, str2, "✓")
  }
}

// http://javadoc.jenkins.io/hudson/PluginManager.html
// http://javadoc.jenkins.io/hudson/PluginWrapper.html
def plugins = Jenkins.instance.pluginManager.plugins.sort()
println "Plugin Count: " + plugins.size() + "\n\n"

println format("Plugins", "Version", "Update Available")
println format("--------------------------------------------------", "--------------------", "----------------")

plugins.each() { plugin ->
  println formatPluginEntry(plugin.getShortName(), plugin.getVersion(), plugin.hasUpdate())
}



// Print one last time so the output isn't cluttered with .each()'s return of the list
println()
