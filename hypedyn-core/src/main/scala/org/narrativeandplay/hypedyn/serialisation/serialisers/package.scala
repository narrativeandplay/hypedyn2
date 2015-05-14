package org.narrativeandplay.hypedyn.serialisation

import org.narrativeandplay.hypedyn.story.{NodeId, NodeContent}
import org.narrativeandplay.hypedyn.story.internal.{Story, Node}

package object serialisers {
  implicit object NodeSerialiser extends Serialisable[Node] {
    /**
     * Returns the serialised representation of an object
     *
     * @param node The object to serialise
     */
    override def serialise(node: Node): AstElement = AstMap("id" -> AstInteger(node.id.value),
                                                         "name" -> AstString(node.name),
                                                         "content" -> NodeContentSerialiser.serialise(node.content),
                                                         "isStart" -> AstBoolean(node.isStartNode))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Node = {
      val data = serialised.asInstanceOf[AstMap]
      val id = data("id").asInstanceOf[AstInteger].i
      val name = data("name").asInstanceOf[AstString].s
      val content = NodeContentSerialiser.deserialise(data("content"))
      val isStart = data("isStart").asInstanceOf[AstBoolean].boolean

      new Node(NodeId(id), name, content, isStart)
    }
  }

  implicit class SerialisableNode(node: Node) {
    def serialise = NodeSerialiser.serialise(node)
  }

  implicit object StorySerialiser extends Serialisable[Story] {
    /**
     * Returns the serialised representation of an object
     *
     * @param story The object to serialise
     */
    override def serialise(story: Story): AstElement = AstMap("title" -> AstString(story.title),
                                                          "author" -> AstString(story.author),
                                                          "description" -> AstString(story.description),
                                                          "nodes" -> AstList(story.nodes map NodeSerialiser.serialise: _*))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Story = {
      val data = serialised.asInstanceOf[AstMap]
      val title = data("title").asInstanceOf[AstString].s
      val author = data("author").asInstanceOf[AstString].s
      val description = data("description").asInstanceOf[AstString].s
      val nodes = data("nodes").asInstanceOf[AstList].toList map NodeSerialiser.deserialise

      new Story(title, author, description, nodes)
    }
  }

  implicit class SerialisableStory(story: Story) {
    def serialise = StorySerialiser.serialise(story)
  }

  implicit object NodeContentSerialiser extends Serialisable[NodeContent] {
    /**
     * Returns the serialised representation of an object
     *
     * @param nodeContent The object to serialise
     */
    override def serialise(nodeContent: NodeContent): AstElement = AstMap("text" -> AstString(nodeContent.text))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): NodeContent =
      NodeContent(serialised.asInstanceOf[AstMap]("text").asInstanceOf[AstString].s)
  }
}
