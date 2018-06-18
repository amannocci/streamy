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

import akka.actor.{Actor, ActorSystem, DiagnosticActorLogging}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer, Supervision}
import io.techcode.streamy.event._
import io.techcode.streamy.util.StreamException

import scala.reflect.io.Directory

/**
  * Abstract plugin implementation based on Actor.
  */
abstract class Plugin(
  val data: PluginData
) extends Actor with DiagnosticActorLogging {

  val decider: Supervision.Decider = { ex =>
    ex match {
      case streamEx: StreamException =>
        log.mdc(if (streamEx.state.isDefined) {
          Map("state" -> streamEx.state.get.toString)
        } else {
          Map.empty[String, String]
        })

        if (streamEx.ex.isDefined) {
          log.error(streamEx.ex.get, streamEx.msg)
        } else {
          log.error(streamEx.msg)
        }
      case _ => log.error(ex, "An error occured")
    }
    Supervision.Resume
  }

  implicit final val system: ActorSystem = context.system
  implicit final val materializer: Materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))(context)

  def receive: Receive = {
    case _ => log.info("Plugin can't handle anything")
  }

  override def preStart(): Unit = {
    system.eventStream.publish(PluginEvent.Loading(data.description.name))
    onStart()
    system.eventStream.publish(PluginEvent.Running(data.description.name))
  }

  override def postStop(): Unit = {
    system.eventStream.publish(PluginEvent.Stopping(data.description.name))
    onStop()
    system.eventStream.publish(PluginEvent.Stopped(data.description.name))
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
