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
package io.techcode.streamy.listener

import akka.actor.{ActorRef, Props}
import com.typesafe.config.ConfigFactory
import io.techcode.streamy.TestSystem
import io.techcode.streamy.event._
import io.techcode.streamy.plugin.PluginState.PluginState
import io.techcode.streamy.plugin.{PluginData, PluginDescription, PluginManager, PluginState}
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.{Interval, Timeout}
import org.scalatest.time.{Seconds, Span}

import scala.reflect.io.Path

/**
  * PluginListener spec.
  */
class PluginListenerSpec extends TestSystem with Eventually {

  val manager = new PluginManager(system, materializer, ConfigFactory.defaultReference().getConfig("streamy"))
  val container = PluginData(
    manager,
    PluginDescription("test", "0.0.0"),
    ConfigFactory.empty(),
    Path(".").toDirectory
  )

  manager.start()

  "PluginListenerSpec" should {
    "detect correctly loading state" in {
      // Register plugin event listener
      var newState = false
      val listener: ActorRef = system.actorOf(Props(classOf[PluginListener], "test", container, (state: PluginState) =>
        if (state == PluginState.Loading) {
          newState = true
        }
      ))
      system.eventStream.subscribe(listener, classOf[PluginEvent])
      system.eventStream.publish(LoadingPluginEvent("test"))

      eventually(timeout = Timeout(Span(120, Seconds)), Interval(Span(1, Seconds))) {
        newState should equal(true)
      }
    }

    "detect correctly running state" in {
      // Register plugin event listener
      var newState = false
      val listener: ActorRef = system.actorOf(Props(classOf[PluginListener], "test", container, (state: PluginState) =>
        if (state == PluginState.Running) {
          newState = true
        }
      ))
      system.eventStream.subscribe(listener, classOf[PluginEvent])
      system.eventStream.publish(LoadingPluginEvent("test"))
      system.eventStream.publish(RunningPluginEvent("test"))

      eventually(timeout = Timeout(Span(120, Seconds)), Interval(Span(1, Seconds))) {
        newState should equal(true)
      }
    }

    "detect correctly stopping state" in {
      // Register plugin event listener
      var newState = false
      val listener: ActorRef = system.actorOf(Props(classOf[PluginListener], "test", container, (state: PluginState) =>
        if (state == PluginState.Stopping) {
          newState = true
        }
      ))
      system.eventStream.subscribe(listener, classOf[PluginEvent])
      system.eventStream.publish(LoadingPluginEvent("test"))
      system.eventStream.publish(RunningPluginEvent("test"))
      system.eventStream.publish(StoppingPluginEvent("test"))

      eventually(timeout = Timeout(Span(120, Seconds)), Interval(Span(1, Seconds))) {
        newState should equal(true)
      }
    }

    "detect correctly stopped state" in {
      // Register plugin event listener
      var newState = false
      val listener: ActorRef = system.actorOf(Props(classOf[PluginListener], "test", container, (state: PluginState) =>
        if (state == PluginState.Stopped) {
          newState = true
        }
      ))
      system.eventStream.subscribe(listener, classOf[PluginEvent])
      system.eventStream.publish(LoadingPluginEvent("test"))
      system.eventStream.publish(RunningPluginEvent("test"))
      system.eventStream.publish(StoppingPluginEvent("test"))
      system.eventStream.publish(StoppedPluginEvent("test"))

      eventually(timeout = Timeout(Span(120, Seconds)), Interval(Span(1, Seconds))) {
        newState should equal(true)
      }
    }
  }

}