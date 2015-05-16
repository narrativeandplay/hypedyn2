package org.narrativeandplay.hypedyn.events

import rx.lang.scala.subjects.{PublishSubject, SerializedSubject}

object EventBus {
  private val eventBus = SerializedSubject(PublishSubject[Event]())

  /**
   * Send an event to the application
   * @param event The event to be sent
   */
  def send(event: Event) = {
    println(event)
    eventBus.onNext(event)
  }



  /**
   * Event stream of all `Event`s
   */
  val Events = eventBus collect { case e: Event => e }


  /**
   * Event stream of all `Request`s
   */
  val Requests = eventBus collect { case e: Request => e }

  val NewNodeRequests = eventBus collect { case e: NewNodeRequest => e }
  val EditNodeRequests = eventBus collect { case e: EditNodeRequest => e }
  val DeleteNodeRequests = eventBus collect { case e: DeleteNodeRequest => e }

  val SaveRequests = eventBus collect { case e: SaveRequest => e }
  val SaveAsRequests = eventBus collect { case e: SaveAsRequest => e }
  val LoadRequests = eventBus collect { case e: LoadRequest => e }

  val CutNodeRequests = eventBus collect { case e: CutNodeRequest => e }
  val CopyNodeRequests = eventBus collect { case e: CopyNodeRequest => e }
  val PasteNodeRequests = eventBus collect { case e: PasteNodeRequest => e }

  val NewStoryRequests = eventBus collect { case e: NewStoryRequest => e }

  val UndoRequests = eventBus collect { case e: UndoRequest => e }
  val RedoRequests = eventBus collect { case e: RedoRequest => e }


  /**
   * Event stream of all `Response`s
   */
  val Responses = eventBus collect { case e: Response => e }

  val NewNodeResponses = eventBus collect { case e: NewNodeResponse => e }
  val EditNodeResponses = eventBus collect { case e: EditNodeResponse => e }
  val DeleteNodeResponses = eventBus collect { case e: DeleteNodeResponse => e }

  val SaveResponses = eventBus collect { case e: SaveResponse => e }
  val SaveAsResponses = eventBus collect { case e: SaveAsResponse => e }
  val LoadResponses = eventBus collect { case e: LoadResponse => e }

  val CutNodeResponses = eventBus collect { case e: CutNodeResponse => e }
  val CopyNodeResponses = eventBus collect { case e: CopyNodeResponse => e }
  val PasteNodeResponses = eventBus collect { case e: PasteNodeResponse => e }

  val NewStoryResponses = eventBus collect { case e: NewStoryResponse => e }

  val UndoResponses = eventBus collect { case e: UndoResponse => e }
  val RedoResponses = eventBus collect { case e: RedoResponse => e }


  /**
   * Event stream of all `Action`s
   */
  val Actions = eventBus collect { case e: Action => e }

  val CreateNodeEvents = eventBus collect { case e: CreateNode => e }
  val UpdateNodeEvents = eventBus collect { case e: UpdateNode => e }
  val DestroyNodeEvents = eventBus collect { case e: DestroyNode => e }

  val SaveDataEvents = eventBus collect { case e: SaveData => e }
  val SaveToFileEvents = eventBus collect { case e: SaveToFile => e }
  val LoadFromFileEvents = eventBus collect { case e: LoadFromFile => e }

  val CreateStoryEvents = eventBus collect { case e: CreateStory => e }


  /**
   * Event stream of all `Completion`s
   */
  val Completions = eventBus collect { case e: Completion => e }

  val NodeCreatedEvents = eventBus collect { case e: NodeCreated => e }
  val NodeUpdatedEvents = eventBus collect { case e: NodeUpdated => e }
  val NodeDestroyedEvents = eventBus collect { case e: NodeDestroyed => e }

  val StorySavedEvents = eventBus collect { case e: StorySaved => e }
  val StoryLoadedEvents = eventBus collect { case e: StoryLoaded => e }
  val DataLoadedEvents = eventBus collect { case e: DataLoaded => e }

  val UiNodeSelectedEvents = eventBus collect { case e: UiNodeSelected => e }
  val UiNodeDeselectedEvents = eventBus collect { case e: UiNodeDeselected => e }
}
