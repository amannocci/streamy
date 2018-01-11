/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2018
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
package io.techcode.streamy.plugin

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.stream.Materializer
import io.techcode.streamy.event.{LoadingPluginEvent, RunningPluginEvent, StoppedPluginEvent, StoppingPluginEvent}

import scala.reflect.io.Directory

/**
  * Abstract plugin implementation based on Actor.
  */
abstract class Plugin(
  val _materializer: Materializer,
  val data: PluginData
) extends Actor with ActorLogging {

  implicit final val materializer: Materializer = _materializer
  implicit final val system: ActorSystem = context.system

  def receive: Receive = {
    case _ => log.info("Plugin can't handle anything")
  }

  override def preStart(): Unit = {
    system.eventStream.publish(LoadingPluginEvent(data.description.name))
    onStart()
    system.eventStream.publish(RunningPluginEvent(data.description.name))
  }

  override def postStop(): Unit = {
    system.eventStream.publish(StoppingPluginEvent(data.description.name))
    onStop()
    system.eventStream.publish(StoppedPluginEvent(data.description.name))
  }

  /**
    * Fired when the plugin is starting.
    */
  def onStart()

  /**
    * Fired when the plugin is stopping.
    */
  def onStop()

  /**
    * Returns the folder that the plugin data's files are located in.
    *
    * @return the folder lazily created if needed.
    */
  def dataFolder: Directory = (data.dataFolder / data.description.name).createDirectory()

}

/**
  * Various plugin state.
  */
object PluginState extends Enumeration {
  type PluginState = Value
  val Unknown, Loading, Running, Stopping, Stopped = Value
}
