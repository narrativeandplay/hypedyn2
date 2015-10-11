package org.narrativeandplay.hypedyn.events

import java.io.File

import org.narrativeandplay.hypedyn.serialisation.AstElement
import org.narrativeandplay.hypedyn.story.rules.{ActionDefinition, ConditionDefinition, Fact, FactId}
import org.narrativeandplay.hypedyn.story.{NodeId, Narrative, Nodal}

/**
 * A generic trait representing an event in the HypeDyn event system
 *
 * HypeDyn relies almost entirely on events to communicate between different layers of the application.
 * The main event flow is as follows:
 *
 * <pre>
 *   Request -> Response -> Action -> Completion
 * </pre>
 *
 * The UI or a plugin would generate a `Request`; the core would generate the appropriate `Response`; the UI or plugin
 * would perform some action/computation and generate an `Action`; the core would perform a resulting action and
 * generate the appropriate `Completion`
 *
 *
 */
sealed trait Event {
  /**
   * Returns the originator of the event
   */
  def src: String
}


/**
 * Requests for information or signifying that the UI/plugin would like something done
 *
 * Generally the UI or plugins send Requests
 */
sealed trait Request extends Event
sealed case class NewNodeRequest(src: String) extends Request
sealed case class EditNodeRequest(id: NodeId, src: String) extends Request
sealed case class DeleteNodeRequest(id: NodeId, src: String) extends Request

sealed case class NewFactRequest(src: String) extends Request
sealed case class EditFactRequest(id: FactId, src: String) extends Request
sealed case class DeleteFactRequest(id: FactId, src: String) extends Request

sealed case class SaveRequest(src: String) extends Request
sealed case class SaveAsRequest(src: String) extends Request
sealed case class LoadRequest(src: String) extends Request

sealed case class CutNodeRequest(id: NodeId, src: String) extends Request
sealed case class CopyNodeRequest(id: NodeId, src: String) extends Request
sealed case class PasteNodeRequest(src: String) extends Request

sealed case class NewStoryRequest(src: String) extends Request
sealed case class EditStoryPropertiesRequest(src: String) extends Request

sealed case class UndoRequest(src: String) extends Request
sealed case class RedoRequest(src: String) extends Request


/**
 * Responses to a request, either containing information, or simply being an acknowledgement of one
 *
 * Generally sent by the core
 */
sealed trait Response extends Event
sealed case class NewNodeResponse(story: Narrative, conditionDefinitions: List[ConditionDefinition], actionDefinitions: List[ActionDefinition], src: String) extends Response
sealed case class EditNodeResponse(node: Nodal, story: Narrative, conditionDefinitions: List[ConditionDefinition], actionDefinitions: List[ActionDefinition], src: String) extends Response
sealed case class DeleteNodeResponse(node: Nodal, story: Narrative, conditionDefinitions: List[ConditionDefinition], actionDefinitions: List[ActionDefinition], src: String) extends Response

sealed case class NewFactResponse(factTypes: List[String], src: String) extends Response
sealed case class EditFactResponse(fact: Fact, factTypes: List[String], src: String) extends Response
sealed case class DeleteFactResponse(fact: Fact, src: String) extends Response

sealed case class SaveResponse(loadedFile: Option[File], src: String) extends Response
sealed case class SaveAsResponse(src: String) extends Response
sealed case class LoadResponse(src: String) extends Response

sealed case class CutNodeResponse(node: Nodal, src: String) extends Response
sealed case class CopyNodeResponse(node: Nodal, src: String) extends Response
sealed case class PasteNodeResponse(src: String) extends Response

sealed case class NewStoryResponse(src: String) extends Response
sealed case class EditStoryPropertiesResponse(story: Narrative, src: String) extends Response

sealed case class UndoResponse(src: String) extends Response
sealed case class RedoResponse(src: String) extends Response


/**
 * Events containing the result of something having been done on the UI, resulting in a request to update something
 *
 * Generally sent by the UI or plugins
 */
sealed trait Action extends Event
sealed case class CreateNode(node: Nodal, src: String) extends Action
sealed case class UpdateNode(node: Nodal, updatedNode: Nodal, src: String) extends Action
sealed case class DestroyNode(node: Nodal, src: String) extends Action

sealed case class CreateFact(fact: Fact, src: String) extends Action
sealed case class UpdateFact(fact: Fact, updatedFact: Fact, src: String) extends Action
sealed case class DestroyFact(fact: Fact, src: String) extends Action

sealed case class SaveData(pluginName: String, data: AstElement, src: String) extends Action
sealed case class SaveToFile(file: File, src: String) extends Action
sealed case class LoadFromFile(file: File, src: String) extends Action

sealed case class CreateStory(title: String = "Untitled",
                              author: String = "",
                              desc: String = "",
                              src: String) extends Action
sealed case class UpdateStoryProperties(title: String,
                                        author: String,
                                        description: String,
                                        metadata: Narrative.Metadata,
                                        src: String) extends Action


/**
 * Events signifying the completion of an Action, mainly to inform plugins that something has happened
 *
 * Generally sent by the core
 */
sealed trait Completion extends Event
sealed case class NodeCreated(node: Nodal, src: String) extends Completion
sealed case class NodeUpdated(node: Nodal, updatedNode: Nodal, src: String) extends Completion
sealed case class NodeDestroyed(node: Nodal, src: String) extends Completion

sealed case class FactCreated(fact: Fact, src: String) extends Completion
sealed case class FactUpdated(fact: Fact, updatedFact: Fact, src: String) extends Completion
sealed case class FactDestroyed(fact: Fact, src: String) extends Completion

sealed case class StorySaved(filename: String, src: String) extends Completion
sealed case class StoryLoaded(story: Narrative, src: String) extends Completion
sealed case class StoryUpdated(story: Narrative, src: String) extends Completion
sealed case class DataLoaded(data: Map[String, AstElement], src: String) extends Completion
sealed case class FileLoaded(filename: String, src: String) extends Completion

sealed case class UiNodeSelected(id: NodeId, src: String) extends Completion
sealed case class UiNodeDeselected(id: NodeId, src: String) extends Completion


sealed trait Notification extends Event
sealed case class FileStatus(isChanged: Boolean, src: String) extends Notification
