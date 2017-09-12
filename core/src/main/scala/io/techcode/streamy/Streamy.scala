/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017
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

import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import io.techcode.streamy.plugin.PluginManager
import io.techcode.streamy.stream.StreamException
import io.techcode.streamy.util.JsonUtil._
import io.techcode.streamy.util.{ConfigConstants, Metrics}
import play.api.libs.json.Json

/**
  * Streamy is an high-performance event processor.
  */
object Streamy extends App {

  // Name of application
  val ApplicationName = "streamy"

  // Actor system
  implicit val system: ActorSystem = ActorSystem(ApplicationName)

  // Logger
  val log = Logger(ApplicationName)

  // Materializer system
  log.info(Json.obj(
    "message" -> "Initializing actor system",
    "type" -> "lifecycle"
  ))
  val decider: Supervision.Decider = { ex =>
    ex match {
      case streamEx: StreamException => log.error(streamEx.toJson)
      case _ => log.error("An error occured", ex)
    }
    Supervision.Resume
  }
  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  // Loading configuration
  log.info(Json.obj(
    "message" -> "Loading configuration with fallback",
    "type" -> "lifecycle"
  ))
  val conf = system.settings.config.resolve()

  // Launch reporter
  if (!conf.getDuration(ConfigConstants.MetricDelay).isNegative) {
    Metrics.reporter(system, log, conf)
  }

  // Attempt to deploy plugins
  val pluginManager = new PluginManager(log, system, materializer, conf)
  pluginManager.start()
}
