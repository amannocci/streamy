/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
