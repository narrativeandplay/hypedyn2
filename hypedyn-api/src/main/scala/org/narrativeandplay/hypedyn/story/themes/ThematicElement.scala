package org.narrativeandplay.hypedyn.story.themes

/**
  * An element is either a motif or a theme. Elements connote themes.
  */
trait ThematicElement {
  /**
    * Returns the ID of the element
    */
  def id: ThematicElementID

  /**
    * Returns the name of the element
    */
  def name: String
}

/**
  * A value type for the ID of an element
  *
  * @param value The integer value of the ID
  */
case class ThematicElementID(value: BigInt) extends AnyVal with Ordered[ThematicElementID] {
  override def compare(that: ThematicElementID): Int = value compare that.value

  /**
    * Returns a ElementID which has it's value incremented by one from the original
    */
  def increment = new ThematicElementID(value + 1)

  /**
    * An alias for `increment`
    */
  def inc = increment

  /**
    * Returns a ElementID which has it's value decremented by one from the original
    */
  def decrement = new ThematicElementID(value - 1)

  /**
    * An alias for `decrement`
    */
  def dec = decrement

  /**
    * Returns true if the ElementID is valid, false otherwise
    *
    * A valid rule id is one whose value is greater than or equal to 0
    */
  def isValid = value >= 0
}

