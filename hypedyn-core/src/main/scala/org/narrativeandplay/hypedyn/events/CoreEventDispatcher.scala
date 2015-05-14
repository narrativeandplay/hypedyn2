package org.narrativeandplay.hypedyn.events

import org.narrativeandplay.hypedyn.plugins.PluginsController
import org.narrativeandplay.hypedyn.serialisation.{IoController, Serialiser, AstMap, AstElement}
import org.narrativeandplay.hypedyn.serialisation.serialisers._
import org.narrativeandplay.hypedyn.story.internal.Story
import org.narrativeandplay.hypedyn.undo._
import org.narrativeandplay.hypedyn.story.StoryController

object CoreEventDispatcher {
  val CoreEventSourceIdentity = "Core"

  EventBus.NewNodeRequests foreach { evt => EventBus.send(NewNodeResponse(CoreEventSourceIdentity)) }
  EventBus.EditNodeRequests foreach { evt =>
    StoryController find evt.id foreach { n => EventBus.send(EditNodeResponse(n, CoreEventSourceIdentity)) }
  }
  EventBus.DeleteNodeRequests foreach { evt =>
    StoryController find evt.id foreach { n => EventBus.send(DeleteNodeResponse(n, CoreEventSourceIdentity)) }
  }

  EventBus.CreateNodeEvents foreach { evt =>
    val created = StoryController.create(evt.node)

    if (evt.src != UndoEventSourceIdentity) {
      UndoController.send(new NodeCreatedChange(created))
    }

    EventBus.send(NodeCreated(created, CoreEventSourceIdentity))
  }
  EventBus.UpdateNodeEvents foreach { evt =>
    val updatedUnupdatedPair = StoryController.update(evt.node, evt.updatedNode)

    updatedUnupdatedPair foreach { case (unupdated, updated) =>
      if (evt.src != UndoEventSourceIdentity) {
        UndoController.send(new NodeUpdatedChange(unupdated, updated))
      }

      EventBus.send(NodeUpdated(unupdated, updated, CoreEventSourceIdentity))
    }
  }
  EventBus.DestroyNodeEvents foreach { evt =>
    val destroyed = StoryController.destroy(evt.node)

    destroyed foreach { n =>
      if (evt.src != UndoEventSourceIdentity) {
        UndoController.send(new NodeDestroyedChange(n))
      }

      EventBus.send(NodeDestroyed(n, CoreEventSourceIdentity))
    }
  }

  EventBus.CutNodeRequests foreach { evt =>
    StoryController find evt.id foreach { n => EventBus.send(CutNodeResponse(n, CoreEventSourceIdentity)) }
  }
  EventBus.CopyNodeRequests foreach { evt =>
    StoryController find evt.id foreach { n => EventBus.send(CopyNodeResponse(n, CoreEventSourceIdentity)) }
  }
  EventBus.PasteNodeRequests foreach { evt => EventBus.send(PasteNodeResponse(CoreEventSourceIdentity)) }

  EventBus.SaveRequests foreach { _ => EventBus.send(SaveResponse(CoreEventSourceIdentity)) }
  EventBus.LoadRequests foreach { _ => EventBus.send(LoadResponse(CoreEventSourceIdentity)) }

  EventBus.SaveDataEvents tumbling PluginsController.plugins.size zip EventBus.SaveToFileEvents foreach {
    case (pluginData, saveFileEvt) =>
      pluginData.foldLeft(Map.empty[String, AstElement])({ case (m, SaveData(pluginName, data, _)) =>
        m + (pluginName -> data)
      }) map { pluginSaveDataMap =>
        AstMap(pluginSaveDataMap.toSeq: _*)
      } foreach { pluginSaveDataAstMap =>
        val storyData = Serialiser serialise StoryController.story
        val saveData = AstMap("story" -> storyData, "plugins" -> pluginSaveDataAstMap)

        IoController.save(Serialiser toString saveData, saveFileEvt.file)

        EventBus.send(StorySaved(CoreEventSourceIdentity))
      }
  }

  EventBus.LoadFromFileEvents foreach { evt =>
    val dataToLoad = IoController load evt.file
    val dataAst = (Serialiser fromString dataToLoad).asInstanceOf[AstMap]

    val pluginData = dataAst("plugins").asInstanceOf[AstMap].toMap

    val story = Serialiser.deserialise[Story](dataAst("story"))
    StoryController load story

    EventBus.send(StoryLoaded(StoryController.story, CoreEventSourceIdentity))
    EventBus.send(DataLoaded(pluginData, CoreEventSourceIdentity))
  }

  EventBus.NewStoryRequests foreach { _ => EventBus.send(NewStoryResponse(CoreEventSourceIdentity)) }
  EventBus.CreateStoryEvents foreach { evt =>
    StoryController.newStory(evt.title, evt.author, evt.desc)

    EventBus.send(StoryLoaded(StoryController.story, CoreEventSourceIdentity))
  }

  EventBus.UndoRequests foreach { _ => EventBus.send(UndoResponse(CoreEventSourceIdentity)) }
  EventBus.RedoRequests foreach { _ => EventBus.send(RedoResponse(CoreEventSourceIdentity)) }
}
