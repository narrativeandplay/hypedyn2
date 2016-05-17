package org.narrativeandplay.hypedyn.events

import java.io.File
import java.nio.file.Files

import org.narrativeandplay.hypedyn.logging.Logger
import org.narrativeandplay.hypedyn.plugins.PluginsController
import org.narrativeandplay.hypedyn.serialisation.serialisers._
import org.narrativeandplay.hypedyn.serialisation.{AstElement, AstMap, IoController, Serialiser}
import org.narrativeandplay.hypedyn.story.StoryController
import org.narrativeandplay.hypedyn.story.internal.Story
import org.narrativeandplay.hypedyn.story.rules.{ActionDefinitions, ConditionDefinitions, Fact}
import org.narrativeandplay.hypedyn.undo._

import scala.util.control.NonFatal

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

  // is this round trip necessary?
  EventBus.NewThemeRequests foreach { _ =>
    EventBus.send(NewThemeResponse(CoreEventSourceIdentity))
  }
  EventBus.EditThemeRequests foreach { evt =>
    StoryController findTheme evt.id foreach { t =>
      EventBus.send(EditThemeResponse(t, CoreEventSourceIdentity))
    }
  }
  EventBus.DeleteThemeRequests foreach { evt =>
    StoryController findTheme evt.id foreach { t =>
      EventBus.send(DeleteThemeResponse(t, CoreEventSourceIdentity))
    }
  }
  // is this round trip necessary?
  EventBus.NewMotifRequests foreach { _ =>
    EventBus.send(NewMotifResponse(CoreEventSourceIdentity))
  }
  EventBus.EditMotifRequests foreach { evt =>
    StoryController findMotif evt.id foreach { m =>
      EventBus.send(EditMotifResponse(m, CoreEventSourceIdentity))
    }
  }
  EventBus.DeleteMotifRequests foreach { evt =>
    StoryController findMotif evt.id foreach { m =>
      EventBus.send(DeleteMotifResponse(m, CoreEventSourceIdentity))
    }
  }
  EventBus.CreateNodeEvents foreach { evt =>
    val created = StoryController.create(evt.node)

    if (evt.src != UndoEventSourceIdentity) {
      UndoableStream.send(new NodeCreatedChange(created, Map.empty))
    }

    EventBus.send(NodeCreated(evt.node, created, CoreEventSourceIdentity))
    EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
  }
  EventBus.UpdateNodeEvents foreach { evt =>
    val updatedUnupdatedPair = StoryController.update(evt.node, evt.updatedNode)

    updatedUnupdatedPair foreach { case (unupdated, updated, changedStartNodeOption) =>
      if (evt.src != UndoEventSourceIdentity) {
        UndoableStream.send(new NodeUpdatedChange(unupdated, updated, changedStartNodeOption))
      }

      EventBus.send(NodeUpdated(unupdated, updated, CoreEventSourceIdentity))
      EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
    }
  }
  EventBus.DestroyNodeEvents foreach { evt =>
    val destroyed = StoryController.destroy(evt.node)

    destroyed foreach { case (destroyedNode, changedNodes) =>
      if (evt.src != UndoEventSourceIdentity) {
        UndoableStream.send(new NodeDestroyedChange(destroyedNode, changedNodes))
      }

      EventBus.send(NodeDestroyed(destroyedNode, CoreEventSourceIdentity))
      EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
    }
  }

  EventBus.CreateFactEvents foreach { evt =>
    val created = StoryController.create(evt.fact)

    UndoableStream.send(new FactCreatedChange(created))

    EventBus.send(FactCreated(created, CoreEventSourceIdentity))
    EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
  }
  EventBus.UpdateFactEvents foreach { evt =>
    val updatedUnupdatedPair = StoryController.update(evt.fact, evt.updatedFact)

    updatedUnupdatedPair foreach { case (unupdated, updated) =>
      UndoableStream.send(new FactUpdatedChange(unupdated, updated))

      EventBus.send(FactUpdated(unupdated, updated, CoreEventSourceIdentity))
      EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
    }
  }
  EventBus.DestroyFactEvents foreach { evt =>
    val destroyed = StoryController.destroy(evt.fact)

    destroyed foreach { f =>
      UndoableStream.send(new FactDestroyedChange(f))

      EventBus.send(FactDestroyed(f, CoreEventSourceIdentity))
      EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
    }
  }

  EventBus.CreateThemeEvents foreach { evt =>
    val created = StoryController.create(evt.theme)

    UndoableStream.send(new ThemeCreatedChange(created)) // undo

    EventBus.send(ThemeCreated(created, CoreEventSourceIdentity))
    EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
  }
  EventBus.UpdateThemeEvents foreach { evt =>
    val updatedUnupdatedPair = StoryController.update(evt.theme, evt.updatedTheme)

    updatedUnupdatedPair foreach { case (unupdated, updated) =>
      UndoableStream.send(new ThemeUpdatedChange(unupdated, updated)) // undo

      EventBus.send(ThemeUpdated(unupdated, updated, CoreEventSourceIdentity))
      EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
    }
  }
  EventBus.DestroyThemeEvents foreach { evt =>
    val destroyed = StoryController.destroy(evt.theme)

    destroyed foreach { t =>
      UndoableStream.send(new ThemeDestroyedChange(t)) // undo

      EventBus.send(ThemeDestroyed(t, CoreEventSourceIdentity))
      EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
    }
  }

  EventBus.CreateMotifEvents foreach { evt =>
    val created = StoryController.create(evt.motif)

    UndoableStream.send(new MotifCreatedChange(created)) // undo

    EventBus.send(MotifCreated(created, CoreEventSourceIdentity))
    EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
  }
  EventBus.UpdateMotifEvents foreach { evt =>
    val updatedUnupdatedPair = StoryController.update(evt.motif, evt.updatedMotif)

    updatedUnupdatedPair foreach { case (unupdated, updated) =>
      UndoableStream.send(new MotifUpdatedChange(unupdated, updated)) // undo

      EventBus.send(MotifUpdated(unupdated, updated, CoreEventSourceIdentity))
      EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
    }
  }
  EventBus.DestroyMotifEvents foreach { evt =>
    val destroyed = StoryController.destroy(evt.motif)

    destroyed foreach { m =>
      UndoableStream.send(new MotifDestroyedChange(m)) // undo

      EventBus.send(MotifDestroyed(m, CoreEventSourceIdentity))
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
    val tmpDir = Files.createTempDirectory("hypedyn2").toFile
    tmpDir.deleteOnExit()

    IoController.copyResourceToFilesystem("export/reader/", tmpDir)

    val storyData = Serialiser serialise StoryController.story
    val saveData = AstMap("story" -> storyData)
    // wrap the JSON in a .js file to allow to avoid cross origin request error running localling in Chrome
    IoController.write("function getStoryData(){\nreturn" + (Serialiser toString saveData) + ";\n};", new File(tmpDir, "story.js"))

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

        IoController.write(Serialiser toString saveData, saveFileEvt.file)

        loadedFile = Some(saveFileEvt.file)

        UndoController.markCurrentPosition()

        EventBus.send(StorySaved(StoryController.story, CoreEventSourceIdentity))
        EventBus.send(FileSaved(saveFileEvt.file, CoreEventSourceIdentity))
      }
  }

  EventBus.LoadFromFileEvents foreach { evt =>
    try {
      val dataToLoad = IoController read evt.file
      val dataAst = Serialiser fromString dataToLoad match {
        case d: AstMap => d
        case e => throw DeserialisationException(s"Expected AstMap in deserialising story, received $e")
      }

      val pluginData = dataAst("plugins") match {
        case d: AstMap => d.toMap
        case e => throw DeserialisationException(s"Expected AstMap in deserialising plugin data, received $e")
      }

      val story = Serialiser.deserialise[Story](dataAst("story"))
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
    val exportDirectory = new File(evt.dir, evt.filename.stripSuffix(".dyn2") + "-export")

    IoController.copyResourceToFilesystem("export/reader/", exportDirectory)

    // save current story to export directory
    val storyData = Serialiser serialise StoryController.story
    val saveData = AstMap("story" -> storyData)
    IoController.write("function getStoryData(){\nreturn" + (Serialiser toString saveData) + ";\n};", new File(exportDirectory, "story.js"))

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
    StoryController.editStory(evt.title, evt.author, evt.description, evt.metadata)

    EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
  }

  EventBus.UndoRequests foreach { _ => EventBus.send(UndoResponse(CoreEventSourceIdentity)) }
  EventBus.RedoRequests foreach { _ => EventBus.send(RedoResponse(CoreEventSourceIdentity)) }
}
