package org.narrativeandplay.hypedyn.api.utils

import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._

import org.bitbucket.inkytonik.kiama.output.PrettyPrinter

import org.narrativeandplay.hypedyn.api.story.NodalContent.RulesetId
import org.narrativeandplay.hypedyn.api.story.NodeId
import org.narrativeandplay.hypedyn.api.story.rules.Actionable.ActionType
import org.narrativeandplay.hypedyn.api.story.rules.Conditional.ConditionType
import org.narrativeandplay.hypedyn.api.story.rules.RuleLike.ParamName
import org.narrativeandplay.hypedyn.api.story.rules.RuleLike.ParamValue.SelectedListValue
import org.narrativeandplay.hypedyn.api.story.rules.{FactId, RuleId}

trait PrettyPrintable extends PrettyPrinter {
  override def any (a : Any) : Doc =
    if (a == null)
      "null"
    else
      a match {
        case v : Vector[_] => list (v.toList, "Vector", any)
        case m : Map[_,_]  => list (m.toList, "Map", any)
        case Nil           => "Nil"
        case l : List[_]   => list (l, "List", any)
        case (l, r)        => any (l) <+> "->" <+> any (r)
        case Some(v)       => list (List(v), "Some", any)
        case None          => "None"
        case NodeId(id)    => s"NodeId ($id)"
        case FactId(id)    => s"FactId ($id)"
        case RuleId(id)    => s"RuleId ($id)"
        case RulesetId(id) => s"RulesetId ($id)"
        case ParamName(name) => list (List(name), "ParamName", any)
        case ActionType(name) => list (List(name), "ActionType", any)
        case ConditionType(name) => list (List(name), "ConditionType", any)
        case SelectedListValue(value) => list (List(value), "SelectedListValue", any)
        case p : Product   =>
          val fieldNames = currentMirror.classSymbol(p.getClass).toType.members.sorted collect {
            case m: MethodSymbol if m.isCaseAccessor => m
          }
          val instanceMirror = currentMirror.reflect(p)
          val fields = fieldNames map { m =>
            m.name.toString -> instanceMirror.reflectMethod(m)()
          }

          if (fields.isEmpty) {
            p.productPrefix
          } else {
            list (
              fields filter { case (fieldName, _) =>
                fieldName != "conditionDefinitions" &&  fieldName != "actionDefinitions"
              },
              p.productPrefix,
              any
            )
          }
        case s : String    => dquotes (text (s))
        case _             => value(a)
      }

  override val defaultIndent = 2
}
