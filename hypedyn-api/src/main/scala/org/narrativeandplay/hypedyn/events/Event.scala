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
sealed trait Event


/**
 * Requests for information or signifying that the UI/plugin would like something done
 *
 * Only the UI or plugins may send Requests
 */
sealed trait Request extends Event
case object NewNodeRequest extends Request
sealed case class EditNodeRequest(id: Long) extends Request
sealed case class DeleteNodeRequest(id: Long) extends Request

case object SaveRequest extends Request
case object LoadRequest extends Request


/**
 * Responses to a request, either containing information, or simply being an acknowledgement of one
 *
 * Sent only by the core
 */
sealed trait Response extends Event
case object NewNodeResponse extends Response
sealed case class EditNodeResponse(node: Nodal) extends Response
sealed case class DeleteNodeResponse(node: Nodal) extends Response

case object SaveResponse extends Response
case object LoadResponse extends Response


/**
 * Events containing the result of something having been done on the UI, resulting in a request to update something
 *
 * Sent only by the UI or plugins
 */
sealed trait Action extends Event
sealed case class CreateNode(node: Nodal) extends Action
sealed case class UpdateNode(node: Nodal, updatedNode: Nodal) extends Action
sealed case class DestroyNode(node: Nodal) extends Action

sealed case class SaveData(pluginName: String, data: AstElement) extends Action
sealed case class SaveStory(file: File) extends Action
sealed case class LoadStory(file: File) extends Action


/**
 * Events signifying the completion of an Action, mainly to inform plugins that something has happened
 *
 * Sent only by the core
 */
sealed trait Completion extends Event
sealed case class NodeCreated(node: Nodal) extends Completion
sealed case class NodeUpdated(node: Nodal) extends Completion
sealed case class NodeDestroyed(node: Nodal) extends Completion

case object StorySaved extends Completion
sealed case class StoryLoaded(story: Narrative) extends Completion
sealed case class DataLoaded(data: Map[String, AstElement]) extends Completion
