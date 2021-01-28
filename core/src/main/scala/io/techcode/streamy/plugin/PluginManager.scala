/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2020
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

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorSystem, CoordinatedShutdown, DiagnosticActorLogging, ExtendedActorSystem, Extension, ExtensionId, Props}
import akka.event.Logging
import akka.pattern.{ask, gracefulStop}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import io.techcode.streamy.config.StreamyConfig
import io.techcode.streamy.event.{ActorListener, PluginEvent}
import pureconfig._
import pureconfig.error.ConfigReaderException
import pureconfig.generic.auto._

import java.net.URL
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{Files, Path}
import java.util.concurrent.{ConcurrentHashMap, TimeUnit}
import java.util.function.BiPredicate
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}
import scala.jdk.javaapi.CollectionConverters.asScala
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * The plugin manager that handle all plugins stuff.
  */
class PluginManager(system: ActorSystem) extends Extension {

  // Configuration
  private val conf: StreamyConfig = ConfigSource.fromConfig(system.settings.config)
    .at("streamy").loadOrThrow[StreamyConfig]

  // Logging system
  private val log = Logging(system, classOf[PluginManager])

  // Actor refs
  private[plugin] val plugins = new ConcurrentHashMap[String, Plugin.Container]

  // Wait for loading phase complete
  private[plugin] val loadingPhaseComplete: Promise[Done] = Promise[Done]()

  /**
    * Get a plugin by his name.
    *
    * @param pluginName plugin name.
    * @return optional plugin container.
    */
  def getPlugin(pluginName: String): Option[Plugin.Container] = Option(plugins.get(pluginName))

  /**
    * Set a plugin by his name.
    *
    * @param pluginName plugin name.
    * @param container  plugin container.
    */
  private[plugin] def setPlugin(pluginName: String, container: Plugin.Container): Plugin.Container =
    plugins.put(pluginName, container)

  /**
    * Start all plugins.
    */
  private def onStart(): Unit = {
    log.info("Starting all plugins")

    // Run plugin listener before anything
    val pluginListener = system.actorOf(Props(classOf[PluginListener], this))
    val waitForListener = Await.ready(ask(pluginListener, NotUsed)(Timeout(20, TimeUnit.SECONDS)), Duration.Inf)
    waitForListener.onComplete {
      case Success(_) =>
        // Retrieve all plugin description
        val pluginDescriptions = getPluginDescriptions

        // Check dependencies & prepare loading
        val toLoads = checkDependencies(pluginDescriptions)

        // Waiting response list
        toLoads.foreach(pluginDescription => {
          try {
            // Merge application configuration and plugin configuration
            val pluginConf = mergeConfig(pluginDescription)

            // Load main plugin class
            val typed = Class.forName(pluginDescription.main.get)

            // Plugin container
            val pluginData = Plugin.Data(
              pluginDescription,
              pluginConf,
              StreamyConfig.DataDirectory
            )

            // Start plugin
            val pluginRef = system.actorOf(Props(typed, pluginData))

            // Add to map
            plugins.put(pluginDescription.name, Plugin.Container(
              description = pluginDescription,
              conf = pluginConf,
              ref = pluginRef
            ))
          } catch {
            case ex: Exception =>
              log.error(ex, "Can't load '{}' plugin", pluginDescription.name)
          }
        })

        // Wait for loading phase complete
        pluginListener ! Done
        Await.ready(loadingPhaseComplete.future, Duration.Inf)
      case Failure(ex) => log.error(ex, "Plugin listener doesn't start properly")
    }
  }

  /**
    * Merge all configurations levels.
    *
    * @param description plugin description.
    * @return configuration merged.
    */
  private def mergeConfig(description: Plugin.Description): Config = {
    (if (conf.plugin.hasPath(description.name)) conf.plugin.getConfig(description.name) else PluginManager.EmptyPluginConfig).resolve()
      .withFallback {
        val pluginConf = StreamyConfig.ConfigurationDirectory.resolve(s"${description.name}.conf")
        if (Files.exists(pluginConf) && Files.isRegularFile(pluginConf) && Files.isReadable(pluginConf)) {
          ConfigFactory.parseFile(pluginConf.toFile).resolve()
        } else {
          PluginManager.EmptyPluginConfig
        }
      }.withFallback(ConfigFactory.parseURL(new URL(s"jar:${description.file.get}!/config.conf")).resolve())
  }

  /**
    * Retrieve all plugins descriptions from jar file.
    *
    * @return all plugins descriptions.
    */
  private def getPluginDescriptions: mutable.AnyRefMap[String, Plugin.Description] = {
    // Retrieve all jar files
    val jarMatcher: BiPredicate[Path, BasicFileAttributes] = (path, _) => path.toString.endsWith(".jar")

    // Attempt to load all plugins
    val pluginDescriptions = mutable.AnyRefMap.empty[String, Plugin.Description]
    Files.find(StreamyConfig.PluginDirectory, 1, jarMatcher).forEach { jar =>
      // Retrieve configuration details
      val conf = ConfigFactory.parseURL(new URL(s"jar:file:${jar.toAbsolutePath.toString}!/plugin.conf"))

      // Attempt to convert configuration to plugin description
      try {
        val description = ConfigSource.fromConfig(conf).loadOrThrow[Plugin.Description].copy(file = Some(jar.toUri.toURL))
        pluginDescriptions += (description.name -> description)
      } catch {
        case _: ConfigReaderException[_] =>
          log.error("Can't load '{}' plugin", jar.getFileName.toString)
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
  private def checkDependencies(pluginDescriptions: mutable.Map[String, Plugin.Description]) = {
    val toLoads = mutable.ArrayBuffer.empty[Plugin.Description]
    for (pluginDescription <- pluginDescriptions.values) {
      if (pluginDescription.main.isDefined) {
        // Condition
        val satisfy = pluginDescription.depends.forall(dependency => {
          if (pluginDescriptions.contains(dependency)) {
            true
          } else {
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

  /**
    * Stop all plugins.
    */
  private def onStop(): Unit = {
    log.info("Stopping all plugins")
    try {
      val signal = Future.sequence(asScala(plugins.values).map { container =>
        // Launch graceful stop
        gracefulStop(container.ref, conf.lifecycle.gracefulTimeout)
      })
      Await.result(signal, conf.lifecycle.shutdownTimeout)
      // All plugins are stopped
    } catch {
      // the actor wasn't stopped within 5 seconds
      case ex: akka.pattern.AskTimeoutException =>
        log.error(ex, "Failed to graceful shutdown")
    }

    // Clear plugins mapping
    plugins.clear()
  }

}

/**
  * Plugin listener for state management.
  *
  * @param pluginManager plugin manager.
  */
private class PluginListener(
  pluginManager: PluginManager
) extends Actor with DiagnosticActorLogging with ActorListener {

  // Check for loading phase completion
  var checkPhaseComplete: Boolean = false

  private def checkPhaseCompletion(): Unit = {
    if (!asScala(pluginManager.plugins.values())
      .exists(c => (c.state == Plugin.State.Loading) || (c.state == Plugin.State.Unknown))) {
      pluginManager.loadingPhaseComplete.success(Done)
    }
  }

  override def receive: Receive = {
    case _: NotUsed => sender() ! NotUsed
    case _: Done =>
      checkPhaseComplete = true
      checkPhaseCompletion()
    case evt: PluginEvent.All =>
      val container = pluginManager.getPlugin(evt.name)
      container.foreach { c =>
        // Set new plugin state
        pluginManager.setPlugin(evt.name, c.copy(ref = sender(), state = evt.toState))
        log.info("Plugin {} is {}", evt.name, evt.toString.toLowerCase())

        // Check if all plugins are ready
        if (checkPhaseComplete) {
          checkPhaseCompletion()
        }
      }
  }

}

/**
  * Plugin manager companion.
  */
object PluginManager extends ExtensionId[PluginManager] {

  // Empty plugin configuration
  val EmptyPluginConfig: Config = ConfigFactory.empty()

  /**
    * Is used by Akka to instantiate the Extension identified by this ExtensionId,
    * internal use only.
    */
  def createExtension(system: ExtendedActorSystem): PluginManager = {
    val pluginManager = new PluginManager(system)
    pluginManager.onStart()
    CoordinatedShutdown(system)
      .addTask(CoordinatedShutdown.PhaseServiceStop, "plugin-manager-shutdown") { () =>
        pluginManager.onStop()
        Future(Done)
      }
    pluginManager
  }

}
