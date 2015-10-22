package org.narrativeandplay.hypedyn.events

import java.io.File
import java.net.URI
import java.nio.file.Files

import org.narrativeandplay.hypedyn.plugins.PluginsController
import org.narrativeandplay.hypedyn.serialisation.{IoController, Serialiser, AstMap, AstElement, ExportController}
import org.narrativeandplay.hypedyn.serialisation.serialisers._
import org.narrativeandplay.hypedyn.story.internal.Story
import org.narrativeandplay.hypedyn.story.rules.{ActionDefinitions, ConditionDefinitions, Fact}
import org.narrativeandplay.hypedyn.undo._
import org.narrativeandplay.hypedyn.story.StoryController

/**
 * Main event dispatcher for the core
 */
object CoreEventDispatcher {
  val CoreEventSourceIdentity = "Core"

  /**
   * Keeps track of whether there is a story loaded via a file load
   */
  private var loadedFile: Option[File] = None

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
    val created = StoryController.create(evt.node)

    if (evt.src != UndoEventSourceIdentity) {
      UndoableStream.send(new NodeCreatedChange(created))
    }

    EventBus.send(NodeCreated(created, CoreEventSourceIdentity))
  }
  EventBus.UpdateNodeEvents foreach { evt =>
    val updatedUnupdatedPair = StoryController.update(evt.node, evt.updatedNode)

    updatedUnupdatedPair foreach { case (unupdated, updated, changedStartNodeOption) =>
      if (evt.src != UndoEventSourceIdentity) {
        UndoableStream.send(new NodeUpdatedChange(unupdated, updated, changedStartNodeOption))
      }

      EventBus.send(NodeUpdated(unupdated, updated, CoreEventSourceIdentity))
    }
  }
  EventBus.DestroyNodeEvents foreach { evt =>
    val destroyed = StoryController.destroy(evt.node)

    destroyed foreach { case (destroyedNode, changedNodes) =>
      if (evt.src != UndoEventSourceIdentity) {
        UndoableStream.send(new NodeDestroyedChange(destroyedNode, changedNodes))
      }

      EventBus.send(NodeDestroyed(destroyedNode, CoreEventSourceIdentity))
    }
  }

  EventBus.CreateFactEvents foreach { evt =>
    val created = StoryController.create(evt.fact)

    if (evt.src != UndoEventSourceIdentity) {
      UndoableStream.send(new FactCreatedChange(created))
    }

    EventBus.send(FactCreated(created, CoreEventSourceIdentity))
  }
  EventBus.UpdateFactEvents foreach { evt =>
    val updatedUnupdatedPair = StoryController.update(evt.fact, evt.updatedFact)

    updatedUnupdatedPair foreach { case (unupdated, updated) =>
      if (evt.src != UndoEventSourceIdentity) {
        UndoableStream.send(new FactUpdatedChange(unupdated, updated))
      }

      EventBus.send(FactUpdated(unupdated, updated, CoreEventSourceIdentity))
    }
  }
  EventBus.DestroyFactEvents foreach { evt =>
    val destroyed = StoryController.destroy(evt.fact)

    destroyed foreach { f =>
      if (evt.src != UndoEventSourceIdentity) {
        UndoableStream.send(new FactDestroyedChange(f))
      }

      EventBus.send(FactDestroyed(f, CoreEventSourceIdentity))
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

  EventBus.ExportRequests foreach { _ => EventBus.send(ExportResponse(loadedFile, CoreEventSourceIdentity)) }
  EventBus.RunStoryRequests foreach { _ => EventBus.send(RunStoryResponse(CoreEventSourceIdentity)) }

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

        EventBus.send(StorySaved(saveFileEvt.file.getName, CoreEventSourceIdentity))
      }
  }

  EventBus.LoadFromFileEvents foreach { evt =>
    val dataToLoad = IoController load evt.file
    val dataAst = (Serialiser fromString dataToLoad).asInstanceOf[AstMap]

    val pluginData = dataAst("plugins").asInstanceOf[AstMap].toMap

    val story = Serialiser.deserialise[Story](dataAst("story"))
    StoryController load story

    loadedFile = Some(evt.file)

    UndoController.clearHistory()
    UndoController.markCurrentPosition()

    EventBus.send(StoryLoaded(StoryController.story, CoreEventSourceIdentity))
    EventBus.send(DataLoaded(pluginData, CoreEventSourceIdentity))
    EventBus.send(FileLoaded(evt.file.getName, CoreEventSourceIdentity))
  }

  EventBus.ExportToFileEvents foreach { evt =>
    val exportDirectory = evt.file;
    val destDirName = "export"

    // create directory and copy over the reader
    ExportController.export(exportDirectory, destDirName);

    // save current story to export directory
    val storyData = Serialiser serialise StoryController.story
    val saveData = AstMap("story" -> storyData)
    IoController.write(Serialiser toString saveData, new File(exportDirectory.getAbsolutePath()+"/"+destDirName+"/story.dyn"))

    // send completion (we're done!)
    EventBus.send(ExportedToFile(CoreEventSourceIdentity))
  }

  EventBus.RunStoryEvents foreach { evt =>
    // create temp directory
    val tempDirectory = Files.createTempDirectory(null)
    tempDirectory.toFile.deleteOnExit
    val destDirName = "hypedyn"

    // create directory and copy over the reader
    ExportController.export(tempDirectory.toFile, destDirName)

    // save current story to temp directory
    val storyData = Serialiser serialise StoryController.story
    val saveData = AstMap("story" -> storyData)
    IoController.write(Serialiser toString saveData, tempDirectory.resolve(destDirName+"/story.dyn").toFile)

    // and launch browser
    if(java.awt.Desktop.isDesktopSupported()){
      try{
        java.awt.Desktop.getDesktop().browse(tempDirectory.resolve(destDirName+"/index.html").toUri)
      } catch {
        case e: Exception => println("CoreEventDispatcher RunStoryEvents exception caught: " + e);
      }
    }

    // send completion (we're done!)
    EventBus.send(RanStory(CoreEventSourceIdentity))
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
    EventBus.send(FileLoaded("Untitled", CoreEventSourceIdentity))
  }
  EventBus.UpdateStoryPropertiesEvents foreach { evt =>
    StoryController.editStory(evt.title, evt.author, evt.description, evt.metadata)

    EventBus.send(StoryUpdated(StoryController.story, CoreEventSourceIdentity))
  }

  EventBus.UndoRequests foreach { _ => EventBus.send(UndoResponse(CoreEventSourceIdentity)) }
  EventBus.RedoRequests foreach { _ => EventBus.send(RedoResponse(CoreEventSourceIdentity)) }
}
