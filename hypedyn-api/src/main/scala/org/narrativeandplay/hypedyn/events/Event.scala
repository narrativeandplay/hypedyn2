package org.narrativeandplay.hypedyn.events

import java.io.File

import org.kiama.output.PrettyPrinter
import org.narrativeandplay.hypedyn.serialisation.AstElement
import org.narrativeandplay.hypedyn.story.NodalContent.RulesetId
import org.narrativeandplay.hypedyn.story.rules._
import org.narrativeandplay.hypedyn.story.themes.{MotifLike, ThematicElementID, ThemeLike}
import org.narrativeandplay.hypedyn.story.{Narrative, Nodal, NodeId}

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
sealed trait Event extends PrettyPrinter {
  /**
   * Returns the originator of the event
   */
  def src: String

  override def any (a : Any) : Doc =
    if (a == null)
      "null"
    else
      a match {
        case v : Vector[_] => list (v.toList, "Vector ", any)
        case m : Map[_,_]  => list (m.toList, "Map ", any)
        case Nil           => "Nil"
        case l : List[_]   => list (l, "List ", any)
        case (l, r)        => any (l) <+> "->" <+> any (r)
        case Some(v)       => s"Some ($v)"
        case None          => "None"
        case NodeId(id)    => s"NodeId ($id)"
        case FactId(id)    => s"FactId ($id)"
        case RuleId(id)    => s"RuleId ($id)"
        case RulesetId(id) => s"RulesetId ($id)"
        case ThematicElementID(id)    => s"ThematicElementId ($id)"
        case p : Product   =>
          val fields = p.getClass.getDeclaredFields map (_.getName) zip p.productIterator.to
          if (fields.length == 0) {
            s"${p.productPrefix}"
          } else {
            list (fields.toList,
                  s"${p.productPrefix} ",
                  any)
          }
        case s : String    => dquotes (text (s))
        case _             => a.toDoc
      }

  override val defaultIndent = 2

  override def toString: String = pretty(any(this))
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

sealed case class NewThemeRequest(src: String) extends Request
sealed case class EditThemeRequest(id: ThematicElementID, src: String) extends Request
sealed case class DeleteThemeRequest(id: ThematicElementID, src: String) extends Request

sealed case class NewMotifRequest(src: String) extends Request
sealed case class EditMotifRequest(id: ThematicElementID, src: String) extends Request
sealed case class DeleteMotifRequest(id: ThematicElementID, src: String) extends Request

sealed case class RecommendationRequest(id: NodeId, src: String) extends Request

sealed case class SaveRequest(src: String) extends Request
sealed case class SaveAsRequest(src: String) extends Request
sealed case class LoadRequest(src: String) extends Request

sealed case class ExportRequest(src: String) extends Request
sealed case class RunRequest(src: String) extends Request

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

sealed case class NewThemeResponse(story: Narrative, src: String) extends Response
sealed case class EditThemeResponse(theme: ThemeLike, story: Narrative, src: String) extends Response
sealed case class DeleteThemeResponse(theme: ThemeLike, src: String) extends Response

sealed case class NewMotifResponse(src: String) extends Response
sealed case class EditMotifResponse(motif: MotifLike, src: String) extends Response
sealed case class DeleteMotifResponse(motif: MotifLike, src: String) extends Response

sealed case class RecommendationResponse(nodeId: NodeId, recommendedNodes: List[(Nodal, Double)], src: String) extends Response

sealed case class SaveResponse(loadedFile: Option[File], src: String) extends Response
sealed case class SaveAsResponse(src: String) extends Response
sealed case class LoadResponse(src: String) extends Response

sealed case class ExportResponse(src: String) extends Response
sealed case class RunResponse(filePath: File, fileToRun: String, src: String) extends Response

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

sealed case class CreateTheme(theme: ThemeLike, src: String) extends Action
sealed case class UpdateTheme(theme: ThemeLike, updatedTheme: ThemeLike, src: String) extends Action
sealed case class DestroyTheme(theme: ThemeLike, src: String) extends Action

sealed case class CreateMotif(motif: MotifLike, src: String) extends Action
sealed case class UpdateMotif(motif: MotifLike, updatedMotif: MotifLike, src: String) extends Action
sealed case class DestroyMotif(motif: MotifLike, src: String) extends Action

sealed case class SaveData(pluginName: String, data: AstElement, src: String) extends Action
sealed case class SaveToFile(file: File, src: String) extends Action
sealed case class LoadFromFile(file: File, src: String) extends Action

sealed case class ExportToFile(dir: File, filename: String, src: String) extends Action
sealed case class RunStory(filePath: File, fileToRun: String, src: String) extends Action

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
sealed case class NodeCreated(originalNodeData: Nodal, node: Nodal, src: String) extends Completion
sealed case class NodeUpdated(node: Nodal, updatedNode: Nodal, src: String) extends Completion
sealed case class NodeDestroyed(node: Nodal, src: String) extends Completion

sealed case class FactCreated(fact: Fact, src: String) extends Completion
sealed case class FactUpdated(fact: Fact, updatedFact: Fact, src: String) extends Completion
sealed case class FactDestroyed(fact: Fact, src: String) extends Completion

sealed case class ThemeCreated(theme: ThemeLike, src: String) extends Completion
sealed case class ThemeUpdated(theme: ThemeLike, updatedTheme: ThemeLike, src: String) extends Completion
sealed case class ThemeDestroyed(theme: ThemeLike, src: String) extends Completion

sealed case class MotifCreated(motif: MotifLike, src: String) extends Completion
sealed case class MotifUpdated(motif: MotifLike, updatedMotif: MotifLike, src: String) extends Completion
sealed case class MotifDestroyed(motif: MotifLike, src: String) extends Completion

sealed case class StorySaved(story: Narrative, src: String) extends Completion
sealed case class StoryLoaded(story: Narrative, src: String) extends Completion
sealed case class StoryUpdated(story: Narrative, src: String) extends Completion
sealed case class DataLoaded(data: Map[String, AstElement], src: String) extends Completion
sealed case class FileSaved(file: File, src: String) extends Completion
sealed case class FileLoaded(fileOption: Option[File], src: String) extends Completion
sealed case class SaveCancelled(src: String) extends Completion

sealed case class StoryExported(src: String) extends Completion
sealed case class StoryRan(src: String) extends Completion

sealed case class UiNodeSelected(id: NodeId, src: String) extends Completion
sealed case class UiNodeDeselected(id: NodeId, src: String) extends Completion

sealed case class UiThemeSelected(id: ThematicElementID, src: String) extends Completion
sealed case class UiThemeDeselected(id: ThematicElementID, src: String) extends Completion

sealed case class UiMotifSelected(id: ThematicElementID, src: String) extends Completion
sealed case class UiMotifDeselected(id: ThematicElementID, src: String) extends Completion


sealed trait Notification extends Event
sealed case class FileStatus(isChanged: Boolean, src: String) extends Notification
sealed case class UndoStatus(isAvailable: Boolean, src: String) extends Notification
sealed case class RedoStatus(isAvailable: Boolean, src: String) extends Notification
sealed case class Error(msg: String, throwable: Throwable, src: String) extends Notification
