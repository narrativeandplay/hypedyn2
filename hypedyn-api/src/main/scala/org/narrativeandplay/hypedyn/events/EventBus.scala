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
  def uiActions = eventBus collect { case e: UIAction => e }
  def actions = eventBus collect { case e: Action => e }
  def completions = eventBus collect { case e: Completion => e }

  /**
   * Request streams
   */
  def newNodeRequests = eventBus collect { case NewNodeRequest => NewNodeRequest }
  def editNodeRequests = eventBus collect { case e: EditNodeRequest => e }
  def deleteNodeRequests = eventBus collect { case e: DeleteNodeRequest => e }

  /**
   * UIAction streams
   */
  def newNodeEvents = eventBus collect { case NewNode => NewNode }
  def editNodeEvents = eventBus collect { case e: EditNode => e }
  def deleteNodeEvents = eventBus collect { case e: DeleteNode => e }

  /**
   * Action streams
   */
  def createNodeEvents = eventBus collect { case e: CreateNode => e }
  def updateNodeEvents = eventBus collect { case e: UpdateNode => e }
  def destroyNodeEvents = eventBus collect { case e: DestroyNode => e }

  /**
   * Completion streams
   */
  def nodeCreatedEvents = eventBus collect { case e: NodeCreated => e }
  def nodeUpdatedEvents = eventBus collect { case e: NodeUpdated => e }
  def nodeDestroyedEvents = eventBus collect { case e: NodeDestroyed => e }
  def nodeSelectedEvents = eventBus collect { case e: NodeSelected => e }
  def nodeDeselectedEvents = eventBus collect { case e: NodeDeselected => e }


  def saveEvents = eventBus.collect { case SaveEvent => SaveEvent }
  def loadEvents = eventBus.collect { case e: LoadEvent => e }
}
