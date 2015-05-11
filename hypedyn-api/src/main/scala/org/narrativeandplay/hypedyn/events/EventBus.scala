package org.narrativeandplay.hypedyn.events

import rx.lang.scala.subjects.{PublishSubject, SerializedSubject}

object EventBus {
  private val _eventBus = SerializedSubject(PublishSubject[Event]())

  /**
   * Send an event to the application
   * @param event The event to be sent
   */
  def send(event: Event) = _eventBus.onNext(event)



  /**
   * Event stream of all `Event`s
   */
  val Events = _eventBus.toObservable


  /**
   * Event stream of all `Request`s
   */
  val Requests = _eventBus collect { case e: Request => e }

  val NewNodeRequests = _eventBus collect { case e: NewNodeRequest => e }
  val EditNodeRequests = _eventBus collect { case e: EditNodeRequest => e }
  val DeleteNodeRequests = _eventBus collect { case e: DeleteNodeRequest => e }

  val SaveRequests = _eventBus collect { case e: SaveRequest => e }
  val LoadRequests = _eventBus collect { case e: LoadRequest => e }


  /**
   * Event stream of all `Response`s
   */
  val Responses = _eventBus collect { case e: Response => e }

  val NewNodeResponses = _eventBus collect { case e: NewNodeResponse => e }
  val EditNodeResponses = _eventBus collect { case e: EditNodeResponse => e }
  val DeleteNodeResponses = _eventBus collect { case e: DeleteNodeResponse => e }

  val SaveResponses = _eventBus collect { case e: SaveResponse => e }
  val LoadResponses = _eventBus collect { case e: LoadResponse => e }


  /**
   * Event stream of all `Action`s
   */
  val Actions = _eventBus collect { case e: Action => e }

  val CreateNodeEvents = _eventBus collect { case e: CreateNode => e }
  val UpdateNodeEvents = _eventBus collect { case e: UpdateNode => e }
  val DestroyNodeEvents = _eventBus collect { case e: DestroyNode => e }

  val SaveDataEvents = _eventBus collect { case e: SaveData => e }
  val SaveStoryEvents = _eventBus collect { case e: SaveStory => e }
  val LoadStoryEvents = _eventBus collect { case e: LoadStory => e }


  /**
   * Event stream of all `Completion`s
   */
  val Completions = _eventBus collect { case e: Completion => e }

  val NodeCreatedEvents = _eventBus collect { case e: NodeCreated => e }
  val NodeUpdatedEvents = _eventBus collect { case e: NodeUpdated => e }
  val NodeDestroyedEvents = _eventBus collect { case e: NodeDestroyed => e }

  val StorySavedEvents = _eventBus collect { case e: StorySaved => e }
  val StoryLoadedEvents = _eventBus collect { case e: StoryLoaded => e }
  val DataLoadedEvents = _eventBus collect { case e: DataLoaded => e }
}
