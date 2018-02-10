// http://javadoc.jenkins.io/hudson/model/Queue.html

// First, grab everything from the queue
def items = Jenkins.instance.getQueue().getItems()

// Iterate through the queue
items.each() { item ->
  // Example of a filter here to keep certain queue items and cancel others
  if(item.task.getFullDisplayName().contains("DoNotCancel")) {
    return
  } else {
    println "Cancelling " + item.task.getFullDisplayName()
    //queue.doCancelItem(item.getId()) // <-------------- Uncomment to actually cancel the items!
  }
}

println ""
