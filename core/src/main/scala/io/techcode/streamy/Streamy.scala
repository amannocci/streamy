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
package io.techcode.streamy

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import io.techcode.streamy.plugin.PluginManager
import io.techcode.streamy.util.logging._
import io.techcode.streamy.util.monitor.DeadLetterMonitor

/**
  * Streamy is an high-performance event processor.
  */
object Streamy extends App {

  // Name of application
  val ApplicationName = "streamy"

  // Actor system
  implicit val system: ActorSystem = ActorSystem(ApplicationName)

  // Logger
  val log = Logging(system, getClass)

  // Materializer system
  log.withContext {
    log.putMDC("type", "lifecyle")
    log.info("Initializing actor system")
  }

  // Register all monitor
  log.withContext {
    log.putMDC("type", "lifecyle")
    log.info("Starting all monitors")
  }
  val deadLetterMonitor = system.actorOf(Props[DeadLetterMonitor], "monitor-dead-letter")

  // Loading configuration
  log.withContext {
    log.putMDC("type", "lifecyle")
    log.info("Loading configuration with fallback")
  }
  val conf = system.settings.config.resolve()

  // Attempt to deploy plugins
  log.withContext {
    log.putMDC("type", "lifecyle")
    log.info("Starting all plugins")
  }
  val pluginManager: ActorRef = system.actorOf(Props(classOf[PluginManager], conf.getConfig("streamy")))

  // Handle dry run
  if (args.length > 0 && args(0).equals("--dry-run")) {
    // Shutdown system
    shutdown()
  } else {
    // Graceful shutdown
    sys.addShutdownHook(shutdown())
  }

  /**
    * Shutdown streamy system.
    */
  def shutdown(): Unit = {
    // Stop all monitors
    log.withContext {
      log.putMDC("type", "lifecyle")
      log.info("Stopping all monitors")
    }
    system.stop(deadLetterMonitor)

    // Stop all plugins
    log.withContext {
      log.putMDC("type", "lifecyle")
      log.info("Stopping all plugins")
    }

    // Stop systems
    system.terminate()
  }

}
