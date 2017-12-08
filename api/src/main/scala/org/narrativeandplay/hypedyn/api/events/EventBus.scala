package org.narrativeandplay.hypedyn.api.events

import rx.lang.scala.subjects.{PublishSubject, SerializedSubject}

import org.narrativeandplay.hypedyn.api.logging.Logger

/**
 * The object managing the event stream of the application
 *
 * All events are sent to this event bus, and all events can be obtained from this bus by tapping
 * into one of its event streams. Each event type gets one stream.
 *
 * TODO: Use a compile-time macro or something to automatically generate event streams by reading the list of events
 */
object EventBus {
  private val eventBus = SerializedSubject(PublishSubject[Event]())

  /**
   * Send an event to the application
   * @param event The event to be sent
   */
  def send(event: Event) = {
    Logger.info(event)
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

  val NewFactRequests = eventBus collect { case e: NewFactRequest => e }
  val EditFactRequests = eventBus collect { case e: EditFactRequest => e }
  val DeleteFactRequests = eventBus collect { case e: DeleteFactRequest => e }

  val SaveRequests = eventBus collect { case e: SaveRequest => e }
  val SaveAsRequests = eventBus collect { case e: SaveAsRequest => e }
  val LoadRequests = eventBus collect { case e: LoadRequest => e }

  val ExportRequests = eventBus collect { case e: ExportRequest => e }
  val RunRequests = eventBus collect { case e: RunRequest => e }

  val CutNodeRequests = eventBus collect { case e: CutNodeRequest => e }
  val CopyNodeRequests = eventBus collect { case e: CopyNodeRequest => e }
  val PasteNodeRequests = eventBus collect { case e: PasteNodeRequest => e }

  val NewStoryRequests = eventBus collect { case e: NewStoryRequest => e }
  val EditStoryPropertiesRequests = eventBus collect { case e: EditStoryPropertiesRequest => e }

  val UndoRequests = eventBus collect { case e: UndoRequest => e }
  val RedoRequests = eventBus collect { case e: RedoRequest => e }

  val ZoomRequests = eventBus collect { case e: ZoomRequest => e }
  val ResetZoomRequests = eventBus collect { case e: ResetZoomRequest => e }


  /**
   * Event stream of all `Response`s
   */
  val Responses = eventBus collect { case e: Response => e }

  val NewNodeResponses = eventBus collect { case e: NewNodeResponse => e }
  val EditNodeResponses = eventBus collect { case e: EditNodeResponse => e }
  val DeleteNodeResponses = eventBus collect { case e: DeleteNodeResponse => e }

  val NewFactResponses = eventBus collect { case e: NewFactResponse => e }
  val EditFactResponses = eventBus collect { case e: EditFactResponse => e }
  val DeleteFactResponses = eventBus collect { case e: DeleteFactResponse => e }

  val SaveResponses = eventBus collect { case e: SaveResponse => e }
  val SaveAsResponses = eventBus collect { case e: SaveAsResponse => e }
  val LoadResponses = eventBus collect { case e: LoadResponse => e }

  val ExportResponses = eventBus collect { case e: ExportResponse => e }
  val RunResponses = eventBus collect { case e: RunResponse => e }

  val CutNodeResponses = eventBus collect { case e: CutNodeResponse => e }
  val CopyNodeResponses = eventBus collect { case e: CopyNodeResponse => e }
  val PasteNodeResponses = eventBus collect { case e: PasteNodeResponse => e }

  val NewStoryResponses = eventBus collect { case e: NewStoryResponse => e }
  val EditStoryPropertiesResponses = eventBus collect { case e: EditStoryPropertiesResponse => e }

  val UndoResponses = eventBus collect { case e: UndoResponse => e }
  val RedoResponses = eventBus collect { case e: RedoResponse => e }

  val ZoomResponses = eventBus collect { case e: ZoomResponse => e }
  val ResetZoomResponses = eventBus collect { case e: ResetZoomResponse => e }


  /**
   * Event stream of all `Action`s
   */
  val Actions = eventBus collect { case e: Action => e }

  val CreateNodeEvents = eventBus collect { case e: CreateNode => e }
  val UpdateNodeEvents = eventBus collect { case e: UpdateNode => e }
  val DestroyNodeEvents = eventBus collect { case e: DestroyNode => e }

  val CreateFactEvents = eventBus collect { case e: CreateFact => e }
  val UpdateFactEvents = eventBus collect { case e: UpdateFact => e }
  val DestroyFactEvents = eventBus collect { case e: DestroyFact => e }

  val SaveDataEvents = eventBus collect { case e: SaveData => e }
  val SaveToFileEvents = eventBus collect { case e: SaveToFile => e }
  val LoadFromFileEvents = eventBus collect { case e: LoadFromFile => e }

  val ExportToFileEvents = eventBus collect { case e: ExportToFile => e }
  val RunStoryEvents = eventBus collect { case e: RunStory => e }

  val CreateStoryEvents = eventBus collect { case e: CreateStory => e }
  val UpdateStoryPropertiesEvents = eventBus collect { case e: UpdateStoryProperties => e }

  val ZoomStoryViewEvents = eventBus collect { case e: ZoomStoryView => e }
  val ResetStoryViewZoomEvents = eventBus collect { case e: ResetStoryViewZoom => e }


  /**
   * Event stream of all `Completion`s
   */
  val Completions = eventBus collect { case e: Completion => e }

  val NodeCreatedEvents = eventBus collect { case e: NodeCreated => e }
  val NodeUpdatedEvents = eventBus collect { case e: NodeUpdated => e }
  val NodeDestroyedEvents = eventBus collect { case e: NodeDestroyed => e }

  val FactCreatedEvents = eventBus collect { case e: FactCreated => e }
  val FactUpdatedEvents = eventBus collect { case e: FactUpdated => e }
  val FactDestroyedEvents = eventBus collect { case e: FactDestroyed => e }

  val StorySavedEvents = eventBus collect { case e: StorySaved => e }
  val StoryUpdatedEvents = eventBus collect { case e: StoryUpdated => e }
  val StoryLoadedEvents = eventBus collect { case e: StoryLoaded => e }
  val DataLoadedEvents = eventBus collect { case e: DataLoaded => e }
  val FileSavedEvents = eventBus collect { case e: FileSaved => e }
  val FileLoadedEvents = eventBus collect { case e: FileLoaded => e }
  val SaveCancelledEvents = eventBus collect { case e: SaveCancelled => e }

  val StoryExportedEvents = eventBus collect { case e: StoryExported => e }
  val StoryRanEvents = eventBus collect { case e: StoryRan => e }

  val UiNodeSelectedEvents = eventBus collect { case e: UiNodeSelected => e }
  val UiNodeDeselectedEvents = eventBus collect { case e: UiNodeDeselected => e }

  val StoryViewZoomedEvents = eventBus collect { case e: StoryViewZoomed => e }
  val StoryViewZoomResetEvents = eventBus collect { case e: StoryViewZoomReset => e }


  /**
   * Event stream of all notifications
   */
  val Notifications = eventBus collect { case e: Notification => e }

  val FileStatusEvents = eventBus collect { case e: FileStatus => e }
  val UndoStatusEvents = eventBus collect { case e: UndoStatus => e }
  val RedoStatusEvents = eventBus collect { case e: RedoStatus => e }
  val ErrorEvents = eventBus collect { case e: Error => e }
}
