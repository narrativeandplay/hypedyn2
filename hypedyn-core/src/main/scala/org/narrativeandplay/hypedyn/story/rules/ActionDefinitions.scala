package org.narrativeandplay.hypedyn.story.rules

object ActionDefinitions {
  private val definitions = List(
    ActionDefinition("LinkTo", "Link to", List(NodeContentAction),
                     List(RuleParameter("node", Nodes))),
    ActionDefinition("ShowPopupNode", "Show node in popup", List(NodeContentAction),
                     List(RuleParameter("node", Nodes))),
    ActionDefinition("UpdateText", "Update text", List(NodeContentAction),
                     List(RuleParameter("text", UserInputString))),
    ActionDefinition("UpdateBooleanFact", "Update true/false fact", List(NodeContentAction, NodeAction, StoryAction),
                     List(RuleParameter("fact", BooleanFacts),
                          RuleParameter("value", ListOfValues("true", "false")))),
    ActionDefinition("UpdateStringFact", "Update text fact", List(NodeContentAction, NodeAction, StoryAction),
                     List(RuleParameter("fact", StringFacts),
                          RuleParameter("value", UserInputString))),
    ActionDefinition("EnableAnywhereLinkToHere", "Enable anywhere link to here", List(NodeAction),
                     List()),
    ActionDefinition("ShowDisabledAnywhereLink", "Show disabled anywhere link", List(NodeAction),
                     List()),
    ActionDefinition("UpdateIntegerFacts", "Update number fact", List(NodeAction, NodeContentAction, StoryAction),
                     List(
                      RuleParameter("fact", IntegerFacts),
                      RuleParameter("updateValue", Union(List(
                        RuleParameter("inputValue", UserInputInteger),
                        RuleParameter("integerFactValue", IntegerFacts),
                        RuleParameter("randomValue", Product(List(
                          RuleParameter("start", Union(List(
                            RuleParameter("startInput", UserInputInteger),
                            RuleParameter("startFact", IntegerFacts)
                          ))),
                          RuleParameter("end", Union(List(
                            RuleParameter("endInput", UserInputInteger),
                            RuleParameter("endFact", IntegerFacts)
                          )))
                        ))),
                        RuleParameter("computation", Product(List(
                          RuleParameter("operator", ListOfValues("+", "-", "x", "/", "%")),
                          RuleParameter("operand1", Union(List(
                            RuleParameter("factOperand1", IntegerFacts),
                            RuleParameter("userOperand1", UserInputInteger)
                          ))),
                          RuleParameter("operand2", Union(List(
                            RuleParameter("factOperand1", IntegerFacts),
                            RuleParameter("userOperand2", UserInputInteger)
                          )))
                        )))
                      )))
                     ))
  )

  def apply() = definitions

}
