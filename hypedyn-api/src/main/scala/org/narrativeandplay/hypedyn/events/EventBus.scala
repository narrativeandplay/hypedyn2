package org.narrativeandplay.hypedyn.events

import rx.lang.scala.subjects.{PublishSubject, SerializedSubject}

object EventBus {
  private val eventBus = SerializedSubject[Event](PublishSubject())

  def send(event: Event): Unit = {
    eventBus.onNext(event)
  }

  def editNodeEvents = eventBus.collect { case e: EditNodeEvent => e }
  def newNodeEvents = eventBus.collect { case NewNodeEvent => NewNodeEvent }
  def deleteNodeEvents = eventBus.collect { case e: DeleteNodeEvent => e }

  def nodeEditedEvents = eventBus.collect { case e: NodeEditedEvent => e }
  def nodeCreatedEvents = eventBus.collect { case e: NodeCreatedEvent => e }
  def nodeDeletedEvents = eventBus.collect { case e: NodeDeletedEvent => e }

  def saveEvents = eventBus.collect { case SaveEvent => SaveEvent }
  def loadEvents = eventBus.collect { case e: LoadEvent => e }
}
