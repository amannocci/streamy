/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2020
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
package io.techcode.streamy.util.monitor

import akka.actor.{ActorSystem, Props}
import io.techcode.streamy.config.StreamyConfig

/**
  * Monitor supervisor.
  */
object Monitors {

  // Dedicated monitor dispatcher
  private val monitorDispatcher = "streamy.dispatcher.monitor"

  /**
    * Run all monitors.
    *
    * @param conf   configuration for monitoring.
    * @param system actor system.
    */
  def runAll(conf: StreamyConfig.Monitor)(implicit system: ActorSystem): Unit = {
    system.actorOf(Props[DeadLetterMonitor](), "monitor-dead-letter")
    if (conf.process.enabled) {
      val actorProps = Props(classOf[ProcessMonitor], conf.process).withDispatcher(monitorDispatcher)
      system.actorOf(actorProps, "monitor-process")
    }
    if (conf.os.enabled) {
      val actorProps = Props(classOf[OsMonitor], conf.os).withDispatcher(monitorDispatcher)
      system.actorOf(actorProps, "monitor-os")
    }
    if (conf.jvm.enabled) {
      val actorProps = Props(classOf[JvmMonitor], conf.jvm).withDispatcher(monitorDispatcher)
      system.actorOf(actorProps, "monitor-jvm")
    }
    if (conf.garbageCollector.enabled) {
      val actorProps = Props(classOf[GarbageCollectorMonitor], conf.garbageCollector).withDispatcher(monitorDispatcher)
      system.actorOf(actorProps, "monitor-garbage-collector")
    }
  }

}
