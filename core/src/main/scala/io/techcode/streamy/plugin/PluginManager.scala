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

import java.net.{URL, URLClassLoader}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern.gracefulStop
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import io.techcode.streamy.event._
import io.techcode.streamy.plugin.PluginState.PluginState
import io.techcode.streamy.util.ConfigConstants
import io.techcode.streamy.util.DurationUtil._
import io.techcode.streamy.util.json._
import org.slf4j.Logger

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.reflect.io.{Directory, File, Path}

/**
  * The plugin manager that handle all plugins stuff.
  */
class PluginManager(log: Logger, system: ActorSystem, materializer: Materializer, conf: Config) {

  // Actor refs
  private[plugin] var _plugins: Map[String, PluginContainer] = Map.empty

  // Plugin class loader
  private var _pluginClassLoader: ClassLoader = _

  // Register plugin event listener
  private val listener: ActorRef = system.actorOf(Props(classOf[PluginsListener], this))
  system.eventStream.subscribe(listener, classOf[PluginEvent])

  /**
    * Start all plugins.
    */
  def start(): Unit = {
    // Retrieve all plugin description
    val pluginDescriptions = getPluginDescriptions

    // Check dependencies & prepare loading
    val toLoads = checkDependencies(pluginDescriptions)

    // Load all valid jars
    _pluginClassLoader = new URLClassLoader(pluginDescriptions.values.map(_.file).toArray, getClass.getClassLoader)

    // Waiting response list
    toLoads.foreach(pluginDescription => {
      try {
        // Merge application configuration and plugin configuration
        val pluginConf = mergeConfig(s"streamy.plugin.${pluginDescription.name}", pluginDescription)

        // Load main plugin class
        val typed = Class.forName(pluginDescription.main.get, true, _pluginClassLoader)

        // Plugin container
        val pluginData = PluginData(
          this,
          pluginDescription,
          pluginConf,
          PluginManager.DataFolder
        )

        // Add to map
        _plugins += (pluginDescription.name -> PluginContainer(
          description = pluginDescription,
          conf = pluginConf
        ))

        // Start plugin
        system.actorOf(Props(
          typed,
          materializer,
          pluginData
        ))
      } catch {
        case ex: Exception => log.error(Json.obj(
          "message" -> s"Can't load '${pluginDescription.name}' plugin",
          "type" -> "lifecycle"
        ), ex)
      }
    })
  }

  /**
    * Stop all plugins.
    */
  def stop(): Unit = {
    try {
      val signal = Future.sequence(_plugins.values.map { data =>
        // Launch graceful stop
        gracefulStop(data.ref, conf.getDuration(ConfigConstants.StreamyLifecycleGracefulTimeout))
      })
      Await.result(signal, conf.getDuration(ConfigConstants.StreamyLifecycleShutdownTimeout))
      // All plugins are stopped
    } catch {
      // the actor wasn't stopped within 5 seconds
      case ex: akka.pattern.AskTimeoutException =>
        log.error(Json.obj(
          "message" -> "Failed to graceful shutdown",
          "type" -> "lifecycle"
        ), ex)
    }
    _plugins = Map.empty
  }

  /**
    * Returns collections of plugins backed by actor ref.
    */
  def plugins: Map[String, PluginContainer] = _plugins

  /**
    * Returns the plugin class loader used to load all plugins.
    *
    * @return plugin class loader.
    */
  def pluginClassLoader: ClassLoader = _pluginClassLoader

  /**
    * Merge all configurations levels.
    *
    * @param path        internal path of plugin conf in streamy conf.
    * @param description plugin description.
    * @return configuration merged.
    */
  private def mergeConfig(path: String, description: PluginDescription): Config = {
    (if (conf.hasPath(path)) conf.getConfig(path) else PluginManager.EmptyPluginConfig).resolve()
      .withFallback((PluginManager.ConfFolder / s"${description.name}.conf")
        .ifFile(f => ConfigFactory.parseFile(f.jfile))
        .getOrElse(PluginManager.EmptyPluginConfig).resolve())
      .withFallback(ConfigFactory.parseURL(new URL(s"jar:${description.file}!/config.conf")).resolve())
  }

  /**
    * Retrieve all plugins descriptions from jar file.
    *
    * @return all plugins descriptions.
    */
  private def getPluginDescriptions: mutable.AnyRefMap[String, PluginDescription] = {
    // Retrieve all jar files
    val jarFiles = PluginManager.PluginFolder.files.filter((x: File) => Path.isExtensionJarOrZip(x.jfile))

    // Attempt to load all plugins
    val pluginDescriptions = mutable.AnyRefMap.empty[String, PluginDescription]
    for (jar <- jarFiles) {
      // Retrieve configuration details
      val conf = ConfigFactory.parseURL(new URL(s"jar:file:${jar.toAbsolute.toString()}!/plugin.conf"))

      // Attempt to convert configuration to plugin description
      try {
        val description = PluginDescription.create(jar.toURL, conf)
        pluginDescriptions += (description.name -> description)
      } catch {
        case _: ConfigException.Missing => log.error(Json.obj(
          "message" -> s"Can't load '${jar.name}' plugin",
          "type" -> "lifecycle"
        ))
      }
    }
    pluginDescriptions
  }

  /**
    * Check dependencies between plugins.
    *
    * @param pluginDescriptions all plugins descriptions.
    * @return list of plugins to load.
    */
  private def checkDependencies(pluginDescriptions: mutable.Map[String, PluginDescription]) = {
    val toLoads = mutable.ArrayBuffer.empty[PluginDescription]
    for (pluginDescription <- pluginDescriptions.values) {
      if (pluginDescription.main.isDefined) {
        // Condition
        val satisfy = pluginDescription.depends.forall(dependency => {
          if (pluginDescriptions.contains(dependency)) {
            true
          } else {
            log.error(Json.obj(
              "message" -> s"Can't load '${pluginDescription.name}' plugin because of unknown dependency '$dependency'",
              "type" -> "lifecycle"
            ))
            false
          }
        })

        // We can load if every dependencies are known
        if (satisfy) {
          toLoads += pluginDescription
        }
      }
    }
    toLoads
  }

}

/**
  * Plugin listener that help to maintain current state plugin.
  */
private class PluginsListener(manager: PluginManager) extends Actor with ActorLogging {

  private def handle(evt: PluginEvent, state: PluginState): Unit = {
    val container = manager._plugins.get(evt.name)
    if (container.isDefined) {
      manager._plugins += (evt.name -> container.get.copy(ref = sender(), state = state))
      log.info(Json.obj(
        "message" -> s"Plugin ${evt.name} ${state.toString.toLowerCase()}",
        "type" -> "plugin",
        "plugin" -> evt.name
      ))
    }
  }

  override def receive: Receive = {
    case evt: LoadingPluginEvent => handle(evt, PluginState.Loading)
    case evt: RunningPluginEvent => handle(evt, PluginState.Running)
    case evt: StoppingPluginEvent => handle(evt, PluginState.Stopping)
    case evt: StoppedPluginEvent => handle(evt, PluginState.Stopped)
  }
}

/**
  * Plugin manager companion.
  */
object PluginManager {

  val PluginFolder: Directory = Path("plugin") toDirectory

  val ConfFolder: Directory = Path("conf") toDirectory

  val DataFolder: Directory = Path("data") toDirectory

  val EmptyPluginConfig: Config = ConfigFactory.empty()

}
