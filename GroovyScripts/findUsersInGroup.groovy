// CONTROL VARIABLES
// Authority (group) to filter
def authority = "Programmers"
// Turn on quiet mode to suppress group listing
def quiet = false
// END CONTROL VARIABLES


// Search all users for those who have the autority listed
def users = hudson.model.User.getAll()
def authorityUsers = users.findAll() { user ->
  user.getAuthorities().contains(authority)
}

println "Users who have the authority \"${authority}\":"

if(authorityUsers.isEmpty()) {
  println "No users found with that authority."
}

authorityUsers.each() { user ->
  print user.getFullName()
  /*user.getProperties().each() { property ->
    println "\t" + property
  }*/
  if (!quiet) {
    println ":"
    user.getAuthorities().each() {
      println "\t" + it
    }
  } else {
    print "\n"
  }
}

return
