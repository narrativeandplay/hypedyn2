package org.narrativeandplay.hypedyn.logging

import org.kiama.output.PrettyPrinter

import org.narrativeandplay.hypedyn.story.NodalContent.RulesetId
import org.narrativeandplay.hypedyn.story.NodeId
import org.narrativeandplay.hypedyn.story.rules.{FactId, RuleId}

object Logger extends PrettyPrinter {
  private val logger = grizzled.slf4j.Logger[this.type]

  override val defaultIndent = 2

  override def any (a : Any) : Doc =
    if (a == null)
      "null"
    else
      a match {
        case v : Vector[_] => list (v.toList, "Vector ", any)
        case m : Map[_,_]  => list (m.toList, "Map ", any)
        case Nil           => "Nil"
        case l : List[_]   => list (l, "List ", any)
        case (l, r)        => any (l) <+> "->" <+> any (r)
        case Some(v)       => s"Some ($v)"
        case None          => "None"
        case NodeId(id)    => s"NodeId ($id)"
        case FactId(id)    => s"FactId ($id)"
        case RuleId(id)    => s"RuleId ($id)"
        case RulesetId(id) => s"RulesetId ($id)"
        case p : Product   =>
          val fields = p.getClass.getDeclaredFields map (_.getName) zip p.productIterator.to
          if (fields.length == 0) {
            s"${p.productPrefix}"
          } else {
            list (fields.toList,
                  s"${p.productPrefix} ",
                  any)
          }
        case s : String    => dquotes (text (s))
        case _             => a.toDoc
      }

  def info(msg: => Any) = logger.info(pretty(any(msg)))
  def info(msg: => Any, t: => Throwable) = logger.info(pretty(any(msg)), t)

  def debug(msg: => Any) = logger.debug(pretty(any(msg)))
  def debug(msg: => Any, t: => Throwable) = logger.debug(pretty(any(msg)), t)

  def error(msg: => Any) = logger.error(pretty(any(msg)))
  def error(msg: => Any, t: => Throwable) = logger.error(pretty(any(msg)), t)
}
