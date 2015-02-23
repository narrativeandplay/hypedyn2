package org.narrativeandplay.hypedyn.plugins.storyviewer.utils

import javafx.event.{Event, EventHandler}

import scala.language.implicitConversions

object SAMConversions {
  implicit def eventLambdaToEventHandler[T <: Event](lambda: T => Unit): EventHandler[T] = new EventHandler[T] {
    override def handle(event: T): Unit = lambda(event)
  }
}
