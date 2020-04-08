package org.narrativeandplay.hypedyn.core.events

import scala.util.control.NonFatal

import better.files._

import org.narrativeandplay.hypedyn.api.events._
import org.narrativeandplay.hypedyn.api.logging.Logger
import org.narrativeandplay.hypedyn.api.serialisation.{AstElement, AstMap, DeserialisationException, Serialiser}
import org.narrativeandplay.hypedyn.api.story.rules.Fact
import org.narrativeandplay.hypedyn.api.undo.UndoableStream
import org.narrativeandplay.hypedyn.api.serialisation.serialisers._
import org.narrativeandplay.hypedyn.api.story.Narrative
import org.narrativeandplay.hypedyn.core.plugins.PluginsController
import org.narrativeandplay.hypedyn.core.serialisation.IoController
import org.narrativeandplay.hypedyn.core.story.StoryController
import org.narrativeandplay.hypedyn.core.story.InterfaceToImplementationConversions._
import org.narrativeandplay.hypedyn.core.story.rules.{ActionDefinitions, ConditionDefinitions}
import org.narrativeandplay.hypedyn.core.undo._

/**
 * Main event dispatcher for the core
 */
object CoreEventDispatcher {
  val CoreEventSourceIdentity = "Core"

  /**
   * Keeps track of whether there is a story loaded via a file load
   */
  private var loadedFile = Option.empty[File]

  EventBus.NewNodeRequests foreach { _ =>
    EventBus.send(NewNodeResponse(StoryController.story, ConditionDefinitions(), ActionDefinitions(), CoreEventSourceIdentity))
  }
  EventBus.EditNodeRequests foreach { evt =>
    StoryController findNode evt.id foreach { n =>
      EventBus.send(EditNodeResponse(n, StoryController.story, ConditionDefinitions(), ActionDefinitions(), CoreEventSourceIdentity))
    }
  }
  EventBus.DeleteNodeRequests foreach { evt =>
    StoryController findNode evt.id foreach { n =>
      EventBus.send(DeleteNodeResponse(n, StoryController.story, ConditionDefinitions(), ActionDefinitions(), CoreEventSourceIdentity))
    }
  }

  EventBus.NewFactRequests foreach { _ =>
    EventBus.send(NewFactResponse(Fact.EnabledFacts, CoreEventSourceIdentity))
  }
  EventBus.EditFactRequests foreach { evt =>
    StoryController findFact evt.id foreach { f =>
      EventBus.send(EditFactResponse(f, Fact.EnabledFacts, CoreEventSourceIdentity))
    }
  }
  EventBus.DeleteFactRequests foreach { evt =>
    StoryController findFact evt.id foreach { f =>
      EventBus.send(DeleteFactResponse(f, CoreEventSourceIdentity))
    }
  }

  EventBus.CreateNodeEvents foreach { evt =>
    val (created, affectedNodes) = StoryController.create(evt.node)

    if (evt.src != UndoEventSourceIdentity) {
      UndoableStream.send(NodeCreatedChange(created, affectedNodes))
    }

    EventBus.send(NodeCreated(evt.node, created, CoreEventSourceIdentity))
    EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
  }
  EventBus.UpdateNodeEvents foreach { evt =>
    val updatedUnupdatedPair = StoryController.update(evt.node, evt.updatedNode)

    updatedUnupdatedPair foreach { case (unupdated, updated, changedStartNodeOption) =>
      if (evt.src != UndoEventSourceIdentity) {
        UndoableStream.send(NodeUpdatedChange(unupdated, updated, changedStartNodeOption))
      }

      EventBus.send(NodeUpdated(unupdated, updated, CoreEventSourceIdentity))
      EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
    }
  }
  EventBus.DestroyNodeEvents foreach { evt =>
    val destroyed = StoryController.destroy(evt.node)

    destroyed foreach { case (destroyedNode, changedNodes) =>
      if (evt.src != UndoEventSourceIdentity) {
        UndoableStream.send(NodeDestroyedChange(destroyedNode, changedNodes))
      }

      EventBus.send(NodeDestroyed(destroyedNode, CoreEventSourceIdentity))
      EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
    }
  }

  EventBus.CreateFactEvents foreach { evt =>
    val created = StoryController.create(evt.fact)

    if (evt.src != UndoEventSourceIdentity) {
      UndoableStream.send(FactCreatedChange(created))
    }

    EventBus.send(FactCreated(created, CoreEventSourceIdentity))
    EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
  }
  EventBus.UpdateFactEvents foreach { evt =>
    val updatedUnupdatedPair = StoryController.update(evt.fact, evt.updatedFact)

    updatedUnupdatedPair foreach { case (unupdated, updated) =>
      if (evt.src != UndoEventSourceIdentity) {
        UndoableStream.send(FactUpdatedChange(unupdated, updated))
      }

      EventBus.send(FactUpdated(unupdated, updated, CoreEventSourceIdentity))
      EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
    }
  }
  EventBus.DestroyFactEvents foreach { evt =>
    val destroyed = StoryController.destroy(evt.fact)

    destroyed foreach { f =>
      if (evt.src != UndoEventSourceIdentity) {
        UndoableStream.send(FactDestroyedChange(f))
      }

      EventBus.send(FactDestroyed(f, CoreEventSourceIdentity))
      EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
    }
  }

  EventBus.CutNodeRequests foreach { evt =>
    StoryController findNode evt.id foreach { n => EventBus.send(CutNodeResponse(n, CoreEventSourceIdentity)) }
  }
  EventBus.CopyNodeRequests foreach { evt =>
    StoryController findNode evt.id foreach { n => EventBus.send(CopyNodeResponse(n, CoreEventSourceIdentity)) }
  }
  EventBus.PasteNodeRequests foreach { evt => EventBus.send(PasteNodeResponse(CoreEventSourceIdentity)) }

  EventBus.SaveRequests foreach { _ => EventBus.send(SaveResponse(loadedFile, CoreEventSourceIdentity)) }
  EventBus.SaveAsRequests foreach { _ => EventBus.send(SaveAsResponse(CoreEventSourceIdentity)) }
  EventBus.LoadRequests foreach { _ => EventBus.send(LoadResponse(CoreEventSourceIdentity)) }

  EventBus.ExportRequests foreach { _ => EventBus.send(ExportResponse(CoreEventSourceIdentity)) }
  EventBus.RunRequests foreach { _ =>
    val tmpDir = File.newTemporaryDirectory("hypedyn2-")
    tmpDir.deleteOnExit()

    IoController.copyResourceFolderToFilesystem("export/reader/", tmpDir)
    StoryController.story.metadata.readerStyle match {
      case Narrative.ReaderStyle.Standard =>
        IoController.copyResourceToFilesystem("export/css/standard.css", tmpDir / "css" / "styling.css")

      case Narrative.ReaderStyle.Fancy =>
        IoController.copyResourceToFilesystem("export/css/fancy.css", tmpDir / "css" / "styling.css")

      case Narrative.ReaderStyle.Custom(cssFile) =>
        val inputFileData = IoController.read(File(cssFile))

        IoController.write(inputFileData, tmpDir / "css" / "styling.css")
    }

    val storyData = Serialiser serialise StoryController.story
    val saveData = AstMap("story" -> storyData)
    // wrap the JSON in a .js file to allow to avoid cross origin request error running localling in Chrome
    IoController.write("function getStoryData(){\nreturn" + (Serialiser render saveData) + ";\n};", tmpDir.createChild("story.js"))

    EventBus.send(RunResponse(tmpDir, "index.html", CoreEventSourceIdentity))
  }

  EventBus.SaveDataEvents tumbling PluginsController.plugins.size zip EventBus.SaveToFileEvents foreach {
    case (pluginData, saveFileEvt) =>
      pluginData.foldLeft(Map.empty[String, AstElement])({ case (m, SaveData(pluginName, data, _)) =>
        m + (pluginName -> data)
      }) map { pluginSaveDataMap =>
        AstMap(pluginSaveDataMap.toSeq: _*)
      } foreach { pluginSaveDataAstMap =>
        val storyData = Serialiser serialise StoryController.story
        val saveData = AstMap("story" -> storyData, "plugins" -> pluginSaveDataAstMap)

        IoController.write(Serialiser render saveData, saveFileEvt.file)

        loadedFile = Some(saveFileEvt.file)

        UndoController.markCurrentPosition()

        EventBus.send(StorySaved(StoryController.story, CoreEventSourceIdentity))
        EventBus.send(FileSaved(saveFileEvt.file, CoreEventSourceIdentity))
      }
  }

  EventBus.LoadFromFileEvents foreach { evt =>
    try {
      val dataToLoad = IoController read evt.file
      val dataAst = Serialiser parse dataToLoad match {
        case d: AstMap => d
        case e => throw DeserialisationException(s"Expected AstMap in deserialising story, received $e")
      }

      val pluginData = dataAst("plugins") match {
        case d: AstMap => d.toMap
        case e => throw DeserialisationException(s"Expected AstMap in deserialising plugin data, received $e")
      }

      val story = Serialiser.deserialise[Narrative](dataAst("story"))
      StoryController load story

      loadedFile = Some(evt.file)

      UndoController.clearHistory()
      UndoController.markCurrentPosition()

      EventBus.send(StoryLoaded(StoryController.story, CoreEventSourceIdentity))
      EventBus.send(DataLoaded(pluginData, CoreEventSourceIdentity))
      EventBus.send(FileLoaded(loadedFile, CoreEventSourceIdentity))
    }
    catch {
      case NonFatal(throwable) =>
        Logger.error("File loading error", throwable)
        EventBus.send(Error("An error occurred while trying to load the story", throwable, CoreEventSourceIdentity))
    }
  }

  EventBus.ExportToFileEvents foreach { evt =>
    val exportDirectory = evt.dir.createChild(evt.filename.stripSuffix(".dyn2") + "-export", asDirectory = true)

    IoController.copyResourceFolderToFilesystem("export/reader/", exportDirectory)
    StoryController.story.metadata.readerStyle match {
      case Narrative.ReaderStyle.Standard =>
        IoController.copyResourceToFilesystem("export/css/standard.css", exportDirectory / "css" / "styling.css")

      case Narrative.ReaderStyle.Fancy =>
        IoController.copyResourceToFilesystem("export/css/fancy.css", exportDirectory / "css" / "styling.css")

      case Narrative.ReaderStyle.Custom(cssFile) =>
        val inputFileData = IoController.read(File(cssFile))

        IoController.write(inputFileData, exportDirectory / "css" / "styling.css")
    }

    // save current story to export directory
    val storyData = Serialiser serialise StoryController.story
    val saveData = AstMap("story" -> storyData)
    IoController.write("function getStoryData(){\nreturn" + (Serialiser render saveData) + ";\n};", exportDirectory.createChild("story.js"))

    // send completion (we're done!)
    EventBus.send(StoryExported(CoreEventSourceIdentity))
  }

  EventBus.NewStoryRequests foreach { _ => EventBus.send(NewStoryResponse(CoreEventSourceIdentity)) }
  EventBus.EditStoryPropertiesRequests foreach { _ =>
    EventBus.send(EditStoryPropertiesResponse(StoryController.story, CoreEventSourceIdentity))
  }
  EventBus.CreateStoryEvents foreach { evt =>
    StoryController.newStory(evt.title, evt.author, evt.desc)

    loadedFile = None

    UndoController.clearHistory()
    UndoController.markCurrentPosition()

    EventBus.send(StoryLoaded(StoryController.story, CoreEventSourceIdentity))
    EventBus.send(FileLoaded(loadedFile, CoreEventSourceIdentity))
  }
  EventBus.UpdateStoryPropertiesEvents foreach { evt =>
    import org.narrativeandplay.hypedyn.core.story.InterfaceToImplementationConversions._

    val oldMetadata = StoryController.story.metadata

    StoryController.editStory(evt.metadata)

    UndoableStream.send(StoryPropertiesChange(oldMetadata, evt.metadata))

    EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
  }

  EventBus.UndoRequests foreach { _ => EventBus.send(UndoResponse(CoreEventSourceIdentity)) }
  EventBus.RedoRequests foreach { _ => EventBus.send(RedoResponse(CoreEventSourceIdentity)) }
}
