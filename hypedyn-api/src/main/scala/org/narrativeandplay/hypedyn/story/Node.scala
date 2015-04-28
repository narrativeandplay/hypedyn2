package org.narrativeandplay.hypedyn.story

trait Node extends Serializable {
  def name: String

  def content: String

  def id: Long
}
