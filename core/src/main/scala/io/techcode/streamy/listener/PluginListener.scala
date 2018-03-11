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

  import context._

  override def preStart(): Unit = {
    // Retrieve plugins
    val plugins = data.pluginManager.plugins

    // Check if plugin reference
    val container = plugins.get(name)
    if (container.isDefined) {
      val state = container.get.state
      onState(state)

      // Determine next state
      state match {
        case PluginState.Loading => become(handleRunning, discardOld = true)
        case PluginState.Running => become(handleStopping, discardOld = true)
        case PluginState.Stopping => become(handleStopped, discardOld = true)
        case PluginState.Stopped => become(receive, discardOld = true)
      }
    }

    // Now listen for next state
    context.system.eventStream.subscribe(self, classOf[PluginEvent])
  }

  /**
    * Handle plugin loading event.
    */
  def handleLoading: Receive = {
    case evt: LoadingPluginEvent =>
      if (evt.name.equals(name)) {
        become(handleRunning, discardOld = true)
        onState(evt.toState)
      }
  }

  /**
    * Handle plugin running event.
    */
  def handleRunning: Receive = {
    case evt: RunningPluginEvent =>
      if (evt.name.equals(name)) {
        become(handleStopping, discardOld = true)
        onState(evt.toState)
      }
  }

  /**
    * Handle plugin stopping event.
    */
  def handleStopping: Receive = {
    case evt: StoppingPluginEvent =>
      if (evt.name.equals(name)) {
        become(handleStopped, discardOld = true)
        onState(evt.toState)
      }
  }

  /**
    * Handle plugin stopped event.
    */
  def handleStopped: Receive = {
    case evt: StoppedPluginEvent =>
      if (evt.name.equals(name)) {
        become(receive, discardOld = true)
        onState(evt.toState)
      }
  }

  /**
    * Handle plugin event.
    */
  def receive: Receive = {
    case evt: LoadingPluginEvent => if (evt.name.equals(name)) handleLoading(evt)
    case evt: RunningPluginEvent => if (evt.name.equals(name)) handleRunning(evt)
    case evt: StoppingPluginEvent => if (evt.name.equals(name)) handleStopping(evt)
    case evt: StoppedPluginEvent => if (evt.name.equals(name)) handleStopped(evt)
  }

}
