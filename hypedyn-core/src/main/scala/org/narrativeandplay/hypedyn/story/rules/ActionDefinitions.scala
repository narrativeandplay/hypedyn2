package org.narrativeandplay.hypedyn.story.rules

object ActionDefinitions {
  import Actionable.ActionType

  private val definitions = List(
    ActionDefinition(ActionType("LinkTo"), "Link to", List(NodeContentAction),
                     List(RuleParameter("node", Nodes))),
    ActionDefinition(ActionType("ShowPopupNode"), "Show node in popup", List(NodeContentAction),
                     List(RuleParameter("node", Nodes))),
    ActionDefinition(ActionType("UpdateText"), "Update text", List(NodeContentAction),
                     List(RuleParameter("text", UserInputString))),
    ActionDefinition(ActionType("UpdateBooleanFact"), "Update true/false fact", List(NodeContentAction, NodeAction, StoryAction),
                     List(RuleParameter("fact", BooleanFacts),
                          RuleParameter("value", ListOfValues("true", "false")))),
    ActionDefinition(ActionType("UpdateStringFact"), "Update text fact", List(NodeContentAction, NodeAction, StoryAction),
                     List(RuleParameter("fact", StringFacts),
                          RuleParameter("value", UserInputString))),
    ActionDefinition(ActionType("EnableAnywhereLinkToHere"), "Enable anywhere link to here", List(NodeAction),
                     List()),
    ActionDefinition(ActionType("ShowDisabledAnywhereLink"), "Show disabled anywhere link", List(NodeAction),
                     List()),
    ActionDefinition(ActionType("UpdateIntegerFacts"), "Update number fact", List(NodeAction, NodeContentAction, StoryAction),
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
                           RuleParameter("operator", ListOfValues("+", "-", "x", "/", "%")),
                           RuleParameter("operand1", Union(Map(
                             "Fact" -> RuleParameter("factOperand1", IntegerFacts),
                             "Input" -> RuleParameter("userOperand1", UserInputInteger)
                           ))),
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
