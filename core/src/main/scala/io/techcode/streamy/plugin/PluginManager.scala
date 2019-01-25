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

import akka.actor.{Actor, DiagnosticActorLogging, Props}
import akka.pattern.gracefulStop
import com.typesafe.config.{Config, ConfigFactory}
import io.techcode.streamy.config.{FolderConfig, LifecycleConfig}
import io.techcode.streamy.event._
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.error.ConfigReaderException

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.reflect.io.{Directory, File, Path}

/**
  * The plugin manager that handle all plugins stuff.
  */
class PluginManager(conf: Config) extends Actor with DiagnosticActorLogging with ActorListener {

  // Actor refs
  private var plugins: Map[String, PluginContainer] = Map.empty

  // Plugin class loader
  private var pluginClassLoader: ClassLoader = _

  // Configuration
  private val lifecycleConfig = loadConfigOrThrow[LifecycleConfig](conf, "lifecycle")
  private val folderConfig = loadConfigOrThrow[FolderConfig](conf, "folder")

  // Common mdc
  private val commonMdc = Map("type" -> "application")

  /**
    * Start all plugins.
    */
  override def preStart(): Unit = {
    log.mdc(commonMdc)
    log.info("Starting all plugins")
    eventStream.subscribe(self, classOf[PluginEvent.All])

    // Retrieve all plugin description
    val pluginDescriptions = getPluginDescriptions

    // Check dependencies & prepare loading
    val toLoads = checkDependencies(pluginDescriptions)

    // Load all valid jars
    pluginClassLoader = new URLClassLoader(pluginDescriptions.values.map(_.file.get).toArray, getClass.getClassLoader)

    // Waiting response list
    toLoads.foreach(pluginDescription => {
      try {
        // Merge application configuration and plugin configuration
        val pluginConf = mergeConfig(s"plugin.${pluginDescription.name}", pluginDescription)

        // Load main plugin class
        val typed = Class.forName(pluginDescription.main.get, true, pluginClassLoader)

        // Plugin container
        val pluginData = PluginData(
          pluginDescription,
          pluginConf,
          Directory(folderConfig.data.toFile)
        )

        // Start plugin
        val pluginRef = context.actorOf(Props(typed, pluginData))

        // Add to map
        plugins += (pluginDescription.name -> PluginContainer(
          description = pluginDescription,
          conf = pluginConf,
          ref = pluginRef
        ))
      } catch {
        case ex: Exception => {
          log.mdc(commonMdc)
          log.error(ex, "Can't load '{}' plugin", pluginDescription.name)
        }
      }
    })
  }

  /**
    * Stop all plugins.
    */
  override def postStop(): Unit = {
    log.mdc(commonMdc)
    log.info("Stopping all plugins")
    try {
      val signal = Future.sequence(context.children.map { ref =>
        // Launch graceful stop
        gracefulStop(ref, lifecycleConfig.gracefulTimeout)
      })
      Await.result(signal, lifecycleConfig.shutdownTimeout)
      // All plugins are stopped
    } catch {
      // the actor wasn't stopped within 5 seconds
      case ex: akka.pattern.AskTimeoutException => {
        log.mdc(commonMdc)
        log.error(ex, "Failed to graceful shutdown")
      }
    }

    // Unregister listener
    super.postStop()

    // Clear plugins mapping
    plugins = Map.empty
  }

  /**
    * Merge all configurations levels.
    *
    * @param path        internal path of plugin conf in streamy conf.
    * @param description plugin description.
    * @return configuration merged.
    */
  private def mergeConfig(path: String, description: PluginDescription): Config = {
    (if (conf.hasPath(path)) conf.getConfig(path) else PluginManager.EmptyPluginConfig).resolve()
      .withFallback((Directory(folderConfig.conf.toFile) / s"${description.name}.conf")
        .ifFile(f => ConfigFactory.parseFile(f.jfile))
        .getOrElse(PluginManager.EmptyPluginConfig).resolve())
      .withFallback(ConfigFactory.parseURL(new URL(s"jar:${description.file.get}!/config.conf")).resolve())
  }

  /**
    * Retrieve all plugins descriptions from jar file.
    *
    * @return all plugins descriptions.
    */
  private def getPluginDescriptions: mutable.AnyRefMap[String, PluginDescription] = {
    // Retrieve all jar files
    val jarFiles = Directory(folderConfig.plugin.toFile).files.filter((x: File) => Path.isExtensionJarOrZip(x.jfile))

    // Attempt to load all plugins
    val pluginDescriptions = mutable.AnyRefMap.empty[String, PluginDescription]
    for (jar <- jarFiles) {
      // Retrieve configuration details
      val conf = ConfigFactory.parseURL(new URL(s"jar:file:${jar.toAbsolute.toString()}!/plugin.conf"))

      // Attempt to convert configuration to plugin description
      try {
        val description = loadConfigOrThrow[PluginDescription](conf).copy(file = Some(jar.toURL))
        pluginDescriptions += (description.name -> description)
      } catch {
        case _: ConfigReaderException[_] =>
          log.mdc(commonMdc)
          log.error("Can't load '{}' plugin", jar.name)
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

            log.mdc(commonMdc)
            log.error("Can't load '{}' plugin because of unknown dependency '{}'", pluginDescription.name, dependency)
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

  override def receive: Receive = {
    case evt: PluginEvent.All =>
      val container = plugins.get(evt.name)
      if (container.isDefined) {
        plugins += (evt.name -> container.get.copy(ref = sender(), state = evt.toState))
        log.mdc(commonMdc)
        log.info("Plugin {} is {}", evt.name, evt.toString.toLowerCase())
      }
  }

}

/**
  * Plugin manager companion.
  */
object PluginManager {

  val EmptyPluginConfig: Config = ConfigFactory.empty()

}
