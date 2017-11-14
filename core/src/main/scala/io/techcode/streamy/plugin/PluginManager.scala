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

import java.net.{URL, URLClassLoader}

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import io.circe._
import io.techcode.streamy.util.JsonUtil._
import org.slf4j.Logger

import scala.collection.mutable
import scala.language.postfixOps
import scala.reflect.io.{Directory, File, Path}

/**
  * The plugin manager that handle all plugins stuff.
  */
class PluginManager(log: Logger, system: ActorSystem, materializer: Materializer, conf: Config) {

  // Actor refs
  private var _plugins: Map[String, ActorRef] = Map.empty

  // Plugin class loader
  private var _pluginClassLoader: ClassLoader = _

  /**
    * Start all plugins.
    */
  def start(): Unit = {
    // Retrieve all plugin description
    val pluginDescriptions = getPluginDescriptions

    // Check dependencies & prepare loading
    val toLoads = checkDependencies(pluginDescriptions)

    // Load all valid jars
    val plugins = mutable.HashMap.empty[String, ActorRef]
    _pluginClassLoader = new URLClassLoader(pluginDescriptions.values.map(_.file).toArray, getClass.getClassLoader)
    toLoads.foreach(pluginDescription => {
      try {
        // Merge application configuration and plugin configuration
        val path = s"plugin.${pluginDescription.name}"
        val pluginConf = (if (conf.hasPath(path)) conf.getConfig(path) else PluginManager.EmptyPluginConfig)
          .withFallback(ConfigFactory.parseURL(new URL(s"jar:${pluginDescription.file}!/config.conf")).resolve())

        // Load main plugin class
        val typed = Class.forName(pluginDescription.main.get, true, _pluginClassLoader)
        val actorRef = system.actorOf(Props(
          typed,
          system,
          materializer,
          pluginDescription,
          pluginConf
        ))
        plugins += (pluginDescription.name -> actorRef)
      } catch {
        case ex: Exception => log.error(Json.obj(
          "message" -> s"Can't load '${pluginDescription.name}' plugin",
          "type" -> "lifecycle"
        ), ex)
      }
    })
    _plugins = plugins.toMap
  }

  /**
    * Stop all plugins.
    */
  def stop(): Unit = _plugins = Map.empty

  /**
    * Returns collections of plugins backed by actor ref.
    */
  def plugins: Map[String, ActorRef] = _plugins

  /**
    * Returns the plugin class loader used to load all plugins.
    *
    * @return plugin class loader.
    */
  def pluginClassLoader: ClassLoader = _pluginClassLoader

  /**
    * Retrieve all plugins descriptions from jar file.
    *
    * @return all plugins descriptions.
    */
  private def getPluginDescriptions: mutable.HashMap[String, PluginDescription] = {
    // Retrieve all jar files
    val jarFiles = PluginManager.PluginFolder.files.filter((x: File) => Path.isExtensionJarOrZip(x.jfile))

    // Attempt to load all plugins
    val pluginDescriptions = mutable.HashMap.empty[String, PluginDescription]
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
  * Plugin manager companion.
  */
object PluginManager {
  val PluginFolder: Directory = Path("plugins") toDirectory

  val EmptyPluginConfig: Config = ConfigFactory.empty()
}
