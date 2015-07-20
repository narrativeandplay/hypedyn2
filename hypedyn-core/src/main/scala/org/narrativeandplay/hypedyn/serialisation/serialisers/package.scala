package org.narrativeandplay.hypedyn.serialisation

import org.narrativeandplay.hypedyn.story.NodeId
import org.narrativeandplay.hypedyn.story.internal.{NodeContent, Story, Node}
import org.narrativeandplay.hypedyn.story.rules._
import org.narrativeandplay.hypedyn.story.rules.internal.{Action, Condition, Rule}

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
                                                         "isStart" -> AstBoolean(node.isStartNode),
                                                         "rules" -> AstList((node.rules map RuleSerialiser.serialise).toSeq: _*))

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
      val rules = data("rules").asInstanceOf[AstList].toList map RuleSerialiser.deserialise

      new Node(NodeId(id), name, content, isStart, rules)
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
    override def serialise(story: Story): AstElement =
      AstMap("title" -> AstString(story.title),
             "author" -> AstString(story.author),
             "description" -> AstString(story.description),
             "nodes" -> AstList(story.nodes map NodeSerialiser.serialise: _*),
             "facts" -> AstList(story.facts map FactSerialiser.serialise: _*),
             "rules" -> AstList(story.rules map RuleSerialiser.serialise: _*))

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
      val facts = data("facts").asInstanceOf[AstList].toList map FactSerialiser.deserialise
      val rules = data("rules").asInstanceOf[AstList].toList map RuleSerialiser.deserialise

      new Story(title, author, description, nodes, facts, rules)
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
    override def serialise(nodeContent: NodeContent): AstElement =
      AstMap("text" -> AstString(nodeContent.text),
             "rulesets" -> AstMap((nodeContent.rulesets map { case (k, v) =>
                s"${k.startIndex.index}:${k.endIndex.index}" -> (RuleSerialiser serialise v)
             }).toSeq: _*))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): NodeContent = {
      val data = serialised.asInstanceOf[AstMap]
      val text = data("text").asInstanceOf[AstString].s
      val rulesets = data("rulesets").asInstanceOf[AstMap].toMap map { case (k, v) =>
          val (start, end) = k split ':' map (_.toInt) match {
            case Array(s, e) => (s, e)
            case unknown =>
              throw DeserialisationException(s"Expected exactly 2 numbers for ruleset indexes, got: $unknown")
          }

          import org.narrativeandplay.hypedyn.story.NodalContent._
          RulesetIndexes(TextIndex(start), TextIndex(end)) -> (RuleSerialiser deserialise v)
      }

      NodeContent(text, rulesets)
    }
  }

  implicit object RuleSerialiser extends Serialisable[Rule] {
    /**
     * Returns the serialised representation of an object
     *
     * @param rule The object to serialise
     */
    override def serialise(rule: Rule): AstElement =
      AstMap("id" -> AstInteger(rule.id.value),
             "name" -> AstString(rule.name),
             "conditionsOp" -> AstString(rule.conditionsOp match {
                                           case And => "and"
                                           case Or => "or"
                                         }),
             "conditions" -> AstList(rule.conditions map ConditionSerialiser.serialise: _*),
             "actions" -> AstList(rule.actions map ActionSerialiser.serialise: _*))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Rule = {
      val data = serialised.asInstanceOf[AstMap]
      val id = RuleId(data("id").asInstanceOf[AstInteger].i)
      val name = data("name").asInstanceOf[AstString].s
      val conditionsOp = data("conditionsOp").asInstanceOf[AstString].s match {
        case "and" => And
        case "or" => Or
        case unknown => throw DeserialisationException(s"Unknown operator for conditons: $unknown")
      }
      val conditions = data("conditions").asInstanceOf[AstList].toList map ConditionSerialiser.deserialise
      val actions = data("actions").asInstanceOf[AstList].toList map ActionSerialiser.deserialise

      Rule(id, name, conditionsOp, conditions, actions)
    }
  }

  implicit object ConditionSerialiser extends Serialisable[Condition] {
    /**
     * Returns the serialised representation of an object
     *
     * @param condition The object to serialise
     */
    override def serialise(condition: Condition): AstElement =
      AstMap("conditionType" -> AstString(condition.conditionType),
             "params" -> AstMap((condition.params map { case (k, v) =>
               k -> AstString(v)
             }).toSeq: _*))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Condition = {
      val data = serialised.asInstanceOf[AstMap]
      val conditionType = data("conditionType").asInstanceOf[AstString].s
      val params = data("params").asInstanceOf[AstMap].toMap map { case (k, v) =>
        k -> v.asInstanceOf[AstString].s
      }

      Condition(conditionType, params)
    }
  }
  
  implicit object ActionSerialiser extends Serialisable[Action] {
    /**
     * Returns the serialised representation of an object
     *
     * @param action The object to serialise
     */
    override def serialise(action: Action): AstElement = AstMap("actionType" -> AstString(action.actionType),
                                                                "params" -> AstMap((action.params map { case (k, v) =>
                                                                    k -> AstString(v)
                                                                }).toSeq: _*))

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Action = {
      val data = serialised.asInstanceOf[AstMap]
      val actionType = data("actionType").asInstanceOf[AstString].s
      val params = data("params").asInstanceOf[AstMap].toMap map { case (k, v) =>
          k -> v.asInstanceOf[AstString].s
      }

      Action(actionType, params)
    }
  }
  
  implicit object FactSerialiser extends Serialisable[Fact] {
    /**
     * Returns the serialised representation of an object
     *
     * @param fact The object to serialise
     */
    override def serialise(fact: Fact): AstElement = {
      val value: AstElement = fact match {
        case IntegerFact(_, _, i) => AstInteger(i)
        case StringFact(_, _, s) => AstString(s)
        case BooleanFact(_, _, b) => AstBoolean(b)
        case IntegerFactList(_, _, is) => AstList(is map serialise: _*)
        case StringFactList(_, _, ss) => AstList(ss map serialise: _*)
        case BooleanFactList(_, _, bs) => AstList(bs map serialise: _*)
      }

      val factType = fact match {
        case _: IntegerFact => "int"
        case _: StringFact => "string"
        case _: BooleanFact => "bool"
        case _: IntegerFactList => "int list"
        case _: StringFactList => "string list"
        case _: BooleanFactList => "bool list"
      }

      AstMap("id" -> AstInteger(fact.id.value),
             "name" -> AstString(fact.name),
             "type" -> AstString(factType),
             "initialValue" -> value)
    }

    /**
     * Returns an object given it's serialised representation
     *
     * @param serialised The serialised form of the object
     */
    override def deserialise(serialised: AstElement): Fact = {
      val data = serialised.asInstanceOf[AstMap]
      val id = FactId(data("id").asInstanceOf[AstInteger].i)
      val name = data("name").asInstanceOf[AstString].s

      (data("type").asInstanceOf[AstString].s, data("initialValue")) match {
        case ("int", value) => IntegerFact(id, name, value.asInstanceOf[AstInteger].i)
        case ("string", value) => StringFact(id, name, value.asInstanceOf[AstString].s)
        case ("bool", value) => BooleanFact(id, name, value.asInstanceOf[AstBoolean].boolean)
        case ("int list", value) =>
          IntegerFactList(id, name,
                          value.asInstanceOf[AstList].toList map deserialise map (_.asInstanceOf[IntegerFact]))
        case ("string list", value) =>
          StringFactList(id, name,
                         value.asInstanceOf[AstList].toList map deserialise map (_.asInstanceOf[StringFact]))
        case ("bool list", value) =>
          BooleanFactList(id, name,
                          value.asInstanceOf[AstList].toList map deserialise map (_.asInstanceOf[BooleanFact]))
        case (factType, value) => throw DeserialisationException(s"Unknown fact type: $factType with value: $value")
      }
    }
  }
}
