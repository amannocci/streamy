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
package io.techcode.streamy.plugin

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import com.typesafe.config.Config
import io.techcode.streamy.util.JsonUtil._
import org.slf4j.Logger
import play.api.libs.json.Json

/**
  * The plugin manager that handle all plugins stuff.
  */
class PluginManager(log: Logger, system: ActorSystem, materializer: Materializer, conf: Config) {

  // Actor ref
  private var _plugins: Map[String, ActorRef] = Map.empty

  /**
    * Start all plugins.
    */
  def start(): Unit = {
    // Build plugins access
    val tmp = Map.newBuilder[String, ActorRef]
    conf.getObject("plugin").forEach((key, value) => {
      try {
        val typed = Class.forName(value.unwrapped().toString)
        val actorRef = system.actorOf(Props(typed, system, materializer, conf.getConfig(s"stream.$key")))
        tmp += (key -> actorRef)
      } catch {
        case ex: Exception => log.error(Json.obj(
          "message" -> s"Can't load '$key' plugin",
          "type" -> "lifecycle"
        ), ex)
      }
    })
    _plugins = tmp.result()
  }

  /**
    * Stop all plugins.
    */
  def stop(): Unit = _plugins = Map.empty

  /**
    * Returns collections of plugins backed by actor ref.
    */
  def plugins: Map[String, ActorRef] = _plugins

}
