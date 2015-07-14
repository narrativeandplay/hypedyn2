package org.narrativeandplay.hypedyn.story.rules

object ConditionDefinitions {
  def apply() = definitions

  private val definitions = List(
    ConditionDefinition("NodeCondition", "Node", List(RuleParameter("node", Nodes),
                                                      RuleParameter("status",
                                                                    ListOfValues("visited",
                                                                                 "not visited",
                                                                                 "is previous",
                                                                                 "is not previous",
                                                                                 "current")))),
    ConditionDefinition("LinkCondition", "Link", List(RuleParameter("link", Links),
                                                      RuleParameter("status", ListOfValues("followed", "not followed")))),
    ConditionDefinition("BooleanFactValue", "True/false fact", List(RuleParameter("fact", BooleanFacts),
                                                                    RuleParameter("state", ListOfValues("true", "false")))),
    ConditionDefinition("IntegerFactComparison", "Number fact", List(RuleParameter("fact", IntegerFacts),
                                                                     RuleParameter("operator", ListOfValues("<",
                                                                                                            ">",
                                                                                                            ">=",
                                                                                                            "<=",
                                                                                                            "==",
                                                                                                            "!=")),
                                                                     RuleParameter("comparisonValue",
                                                                                   Union(Map(
                                                                                     "Input" -> RuleParameter("input", UserInputInteger),
                                                                                     "Fact" -> RuleParameter("otherFact", IntegerFacts)))))
    )
  )
}
