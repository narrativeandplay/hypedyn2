package org.narrativeandplay.hypedyn.story

/**
 * A generic trait representing an element of a story
 *
 * @tparam T The type of story element being implemented
 */
trait NarrativeElement[T <: NarrativeElement[T]] {
  self: T =>
}
