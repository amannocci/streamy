package io.techcode.streamy.listener

import akka.actor.{Actor, ActorLogging}
import io.techcode.streamy.event._
import io.techcode.streamy.plugin.PluginState.PluginState
import io.techcode.streamy.plugin.{PluginData, PluginState}

/**
  * Represent a specific plugin listener.
  */
class PluginListener(
  name: String,
  data: PluginData,
  onState: PluginState => Unit
) extends Actor with ActorLogging {

  // Last state of the plugin
  private var lastState: PluginState = PluginState.Unknown

  // Detect initial state
  {
    // Retrieve plugins
    val plugins = data.pluginManager.plugins

    // Check if plugin metric reference
    val container = plugins.get(name)
    if (container.isDefined) {
      lastState = container.get.state
      onState(lastState)
      context.system.eventStream.subscribe(self, classOf[PluginEvent])
    }
  }

  /**
    * Handle plugin state update.
    *
    * @param evt   plugin event involved.
    * @param state plugin state update.
    */
  private def handle(evt: PluginEvent, state: PluginState): Unit = {
    if (data.pluginManager.plugins.contains(name) && lastState != state) {
      lastState = state
      onState(state)
    }
  }

  def receive: Receive = {
    case evt: LoadingPluginEvent => handle(evt, PluginState.Loading)
    case evt: RunningPluginEvent => handle(evt, PluginState.Running)
    case evt: StoppingPluginEvent => handle(evt, PluginState.Stopping)
    case evt: StoppedPluginEvent => handle(evt, PluginState.Stopped)
  }

}
