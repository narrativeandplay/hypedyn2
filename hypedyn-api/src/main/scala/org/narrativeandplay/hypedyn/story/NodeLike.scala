package org.narrativeandplay.hypedyn.story

trait NodeLike extends Serializable {
  def name: String

  def content: String

  def id: Long
}
