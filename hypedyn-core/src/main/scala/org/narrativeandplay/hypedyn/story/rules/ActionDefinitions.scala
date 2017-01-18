package org.narrativeandplay.hypedyn.story.rules

import org.narrativeandplay.hypedyn.story.rules.ParameterValues._

/**
 * Object holding action definitions
 */
object ActionDefinitions {
  import Actionable.ActionType
  import ActionLocationType.{NodeContentAction, NodeAction, StoryAction}

  private val definitions = List(
    ActionDefinition(ActionType("LinkTo"), "Follow link to", List(NodeContentAction),
                     true,
                     List(RuleParameter("node", Nodes))),
    ActionDefinition(ActionType("ShowPopupNode"), "Show node in popup", List(NodeContentAction),
                     true,
                     List(RuleParameter("node", Nodes))),
    ActionDefinition(ActionType("UpdateText"), "Update fragment text", List(NodeContentAction),
                     false,
                     List(RuleParameter("text", Union(Map(
                      "Input" -> RuleParameter("textInput", UserInputString),
                      "String Fact" -> RuleParameter("stringFactValue", StringFacts),
                      "Number Fact" -> RuleParameter("NumberFactValue", IntegerFacts)
                     ))))),
    ActionDefinition(ActionType("UpdateBooleanFact"), "Update true/false fact", List(NodeContentAction, NodeAction, StoryAction),
                     true,
                     List(RuleParameter("fact", BooleanFacts),
                          RuleParameter("value", ListOfValues("true", "false")))),
    ActionDefinition(ActionType("UpdateStringFact"), "Update string fact", List(NodeContentAction, NodeAction, StoryAction),
                     true,
                     List(RuleParameter("fact", StringFacts),
                          RuleParameter("value", UserInputString))),
    ActionDefinition(ActionType("EnableAnywhereLinkToHere"), "Enable anywhere link to here", List(NodeAction),
                     false,
                     List()),
    ActionDefinition(ActionType("ShowDisabledAnywhereLink"), "Show disabled anywhere link", List(NodeAction),
                     false,
                     List()),
    ActionDefinition(ActionType("UpdateIntegerFacts"), "Update number fact", List(NodeAction, NodeContentAction, StoryAction),
                     true,
                     List(
                       RuleParameter("fact", IntegerFacts),
                       RuleParameter("updateValue", Union(Map(
                         "Input" -> RuleParameter("inputValue", UserInputInteger),
                         "Fact" -> RuleParameter("integerFactValue", IntegerFacts),
                         "Random" -> RuleParameter("randomValue", Product(List(
                           RuleParameter("start", Union(Map(
                             "Input" -> RuleParameter("startInput", UserInputInteger),
                             "Fact" -> RuleParameter("startFact", IntegerFacts)
                           ))),
                           RuleParameter("end", Union(Map(
                             "Input" -> RuleParameter("endInput", UserInputInteger),
                             "Fact" -> RuleParameter("endFact", IntegerFacts)
                           )))
                         ))),
                         "Math" -> RuleParameter("computation", Product(List(
                           RuleParameter("operand1", Union(Map(
                             "Fact" -> RuleParameter("factOperand1", IntegerFacts),
                             "Input" -> RuleParameter("userOperand1", UserInputInteger)
                           ))),
                           RuleParameter("operator", ListOfValues("+", "-", "x", "/", "%")),
                           RuleParameter("operand2", Union(Map(
                             "Fact" -> RuleParameter("factOperand2", IntegerFacts),
                             "Input" -> RuleParameter("userOperand2", UserInputInteger)
                           )))
                         )))
                       )))
                     ))
  )

  def apply() = definitions

}
