package org.narrativeandplay.hypedyn.events

import java.io.File

import org.narrativeandplay.hypedyn.serialisation.AstElement
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal}

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
  def source: String
}


/**
 * Requests for information or signifying that the UI/plugin would like something done
 *
 * Only the UI or plugins may send Requests
 */
sealed trait Request extends Event
sealed case class NewNodeRequest(source: String) extends Request
sealed case class EditNodeRequest(id: Long, source: String) extends Request
sealed case class DeleteNodeRequest(id: Long, source: String) extends Request

sealed case class SaveRequest(source: String) extends Request
sealed case class LoadRequest(source: String) extends Request


/**
 * Responses to a request, either containing information, or simply being an acknowledgement of one
 *
 * Sent only by the core
 */
sealed trait Response extends Event
sealed case class NewNodeResponse(source: String) extends Response
sealed case class EditNodeResponse(node: Nodal, source: String) extends Response
sealed case class DeleteNodeResponse(node: Nodal, source: String) extends Response

sealed case class SaveResponse(source: String) extends Response
sealed case class LoadResponse(source: String) extends Response


/**
 * Events containing the result of something having been done on the UI, resulting in a request to update something
 *
 * Sent only by the UI or plugins
 */
sealed trait Action extends Event
sealed case class CreateNode(node: Nodal, source: String) extends Action
sealed case class UpdateNode(node: Nodal, updatedNode: Nodal, source: String) extends Action
sealed case class DestroyNode(node: Nodal, source: String) extends Action

sealed case class SaveData(pluginName: String, data: AstElement, source: String) extends Action
sealed case class SaveStory(file: File, source: String) extends Action
sealed case class LoadStory(file: File, source: String) extends Action


/**
 * Events signifying the completion of an Action, mainly to inform plugins that something has happened
 *
 * Sent only by the core
 */
sealed trait Completion extends Event
sealed case class NodeCreated(node: Nodal, source: String) extends Completion
sealed case class NodeUpdated(node: Nodal, source: String) extends Completion
sealed case class NodeDestroyed(node: Nodal, source: String) extends Completion

sealed case class StorySaved(source: String) extends Completion
sealed case class StoryLoaded(story: Narrative, source: String) extends Completion
sealed case class DataLoaded(data: Map[String, AstElement], source: String) extends Completion
