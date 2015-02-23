package org.narrativeandplay.hypedyn.plugins.storyviewer.utils

case class UnorderedPair[T](_1: T, _2: T) {
  override def equals(that: Any) = that match {
    case that: UnorderedPair[T] => that.canEqual(this) &&
      ((this._1 == that._1 && this._2 == that._2) || (this._1 == that._2 && this._2 == that._1))
    case _ => false
  }

  def contains(elem: T) = elem == _1 || elem == _2

  override def canEqual(that: Any) = that.isInstanceOf[UnorderedPair[T]]
  override def hashCode = _1.hashCode + _2.hashCode
}
