package org.narrativeandplay.hypedyn.story

sealed class Node(var name: String, var content: String, val id: Long = Node.firstUnusedId) {
  Node.firstUnusedId = math.max(Node.firstUnusedId, id + 1)
}

object Node {
  var firstUnusedId = 0.toLong
}
