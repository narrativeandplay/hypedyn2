package org.narrativeandplay.hypedyn.story.rules

import org.narrativeandplay.hypedyn.story.rules.ParameterValues._

/**
 * Object holding condition definitions
 */
object ConditionDefinitions {
  import Conditional.ConditionType

  def apply() = definitions

  private val definitions = List(
    ConditionDefinition(ConditionType("NodeCondition"), "Node", List(RuleParameter("node", Nodes),
                                                                     RuleParameter("status",
                                                                                   ListOfValues("visited",
                                                                                                "not visited",
                                                                                                "is previous",
                                                                                                "is not previous",
                                                                                                "current")))),
    ConditionDefinition(ConditionType("LinkCondition"), "Link", List(RuleParameter("link", Links),
                                                                     RuleParameter("status",
                                                                                   ListOfValues("followed",
                                                                                                "not followed")))),
    ConditionDefinition(ConditionType("BooleanFactValue"), "True/false fact", List(RuleParameter("fact", BooleanFacts),
                                                                                   RuleParameter("state",
                                                                                                 ListOfValues(
                                                                                                   "true",
                                                                                                   "false")))),
    ConditionDefinition(ConditionType("IntegerFactComparison"), "Number fact", List(RuleParameter("fact", IntegerFacts),
                                                                                    RuleParameter("operator",
                                                                                                  ListOfValues(
                                                                                                    "<",
                                                                                                    ">",
                                                                                                    ">=",
                                                                                                    "<=",
                                                                                                    "=",
                                                                                                    "not =")),
                                                                                    RuleParameter("comparisonValue",
                                                                                                  Union(Map(
                                                                                                    "Input" -> RuleParameter("input", UserInputInteger),
                                                                                                    "Fact" -> RuleParameter("otherFact", IntegerFacts)))))
    )
  )
}
