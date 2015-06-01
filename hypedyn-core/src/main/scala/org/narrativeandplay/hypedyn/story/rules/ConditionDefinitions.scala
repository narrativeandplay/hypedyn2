package org.narrativeandplay.hypedyn.story.rules

object ConditionDefinitions {
  def apply() = definitions

  private val definitions = List(
    ConditionDefinition("NodeCondition", List(RuleParameter("node", Nodes),
                                              RuleParameter("status",
                                                            ListOfValues("visited",
                                                                         "not visited",
                                                                         "is previous",
                                                                         "is not previous",
                                                                         "current")))),
    ConditionDefinition("LinkCondition", List(RuleParameter("link", Links),
                                              RuleParameter("status", ListOfValues("followed", "not followed")))),
    ConditionDefinition("BooleanFactValue", List(RuleParameter("fact", BooleanFacts),
                                                 RuleParameter("state", ListOfValues("true", "false")))),
    ConditionDefinition("IntegerFactComparison", List(RuleParameter("fact", IntegerFacts),
                                                      RuleParameter("operator", ListOfValues("<",
                                                                                             ">",
                                                                                             ">=",
                                                                                             "<=",
                                                                                             "==",
                                                                                             "!=")),
                                                      RuleParameter("comparisonValue",
                                                                    Union(List(
                                                                      RuleParameter("input", UserInput),
                                                                      RuleParameter("otherFact", IntegerFacts)))))
    )
  )
}
