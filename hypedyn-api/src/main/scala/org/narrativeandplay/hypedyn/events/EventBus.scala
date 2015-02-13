package org.narrativeandplay.hypedyn.events

import rx.lang.scala.subjects.{PublishSubject, SerializedSubject}

object EventBus {
  private val eventBus = SerializedSubject[Event](PublishSubject())

  def send(event: Event): Unit = {
    eventBus.onNext(event)
  }

  /**
   * All Observable streams are considered private API, no plugin should be using the streams, and they are subject
   * to breaking at any time
   */
  def requests = eventBus collect { case e: Request => e }
  def actions = eventBus collect { case e: Action => e }
  def completions = eventBus collect { case e: Completion => e }

  def createNodeRequests = eventBus collect { case CreateNodeRequest => CreateNodeRequest }
  def editNodeRequests = eventBus collect { case EditNodeRequest => EditNodeRequest }
  def deleteNodeRequests = eventBus collect { case DeleteNodeRequest => DeleteNodeRequest }

  def createNodeEvents = eventBus collect { case e: CreateNode => e }
  def editNodeEvents = eventBus collect { case e: EditNode => e }
  def deleteNodeEvents = eventBus collect { case e: DeleteNode => e }

  def nodeCreatedEvents = eventBus collect { case e: NodeCreated => e }
  def nodeEditedEvents = eventBus collect { case e: NodeEdited => e }
  def nodeDeletedEvents = eventBus collect { case e: NodeDeleted => e }


  def saveEvents = eventBus.collect { case SaveEvent => SaveEvent }
  def loadEvents = eventBus.collect { case e: LoadEvent => e }
}
