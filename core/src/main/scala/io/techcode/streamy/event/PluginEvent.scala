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
package io.techcode.streamy.event

import akka.actor.DeadLetterSuppression
import io.techcode.streamy.plugin.PluginState
import io.techcode.streamy.plugin.PluginState.PluginState

/**
  * All plugin events.
  */
object PluginEvent {

  /**
    * Represent an app event.
    */
  abstract class All(val name: String) extends DeadLetterSuppression {

    /**
      * Convert a plugin event to a plugin lifecycle state.
      *
      * @return plugin lifecycle state.
      */
    def toState: PluginState

    override def toString: String = toState.toString

  }

  /**
    * Represent an plugin loading event.
    * This event is fired when a plugin is loading.
    */
  case class Loading(override val name: String) extends All(name) {
    def toState: PluginState = PluginState.Loading
  }

  /**
    * Represent an plugin running event.
    * This event is fired when a plugin is running.
    */
  case class Running(override val name: String) extends All(name) {
    def toState: PluginState = PluginState.Running
  }

  /**
    * Represent an plugin stopping event.
    * This event is fired when a plugin is stopping.
    */
  case class Stopping(override val name: String) extends All(name) {
    def toState: PluginState = PluginState.Stopping
  }

  /**
    * Represent an plugin stopping event.
    * This event is fired when a plugin is stopped.
    */
  case class Stopped(override val name: String) extends All(name) {
    def toState: PluginState = PluginState.Stopped
  }

}
