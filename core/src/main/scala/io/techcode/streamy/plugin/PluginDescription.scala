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

import java.net.URL

import com.typesafe.config.Config

import scala.collection.JavaConverters._

/**
  * Represents some plugin informations.
  */
case class PluginDescription(
  name: String,
  version: String,
  main: Option[String] = Option.empty,
  authors: Seq[String] = Seq.empty,
  website: Option[String] = None,
  depends: Seq[String] = Seq.empty,
  file: URL
)

/**
  * Plugin description factory.
  */
object PluginDescription {

  /**
    * Create a new plugin description from config.
    * We use this method instead of `ConfigBeanFactory` for compatibility reason.
    *
    * @param file   location of plugin file.
    * @param config config involved.
    * @return plugin description.
    */
  def create(file: URL, config: Config): PluginDescription = PluginDescription(
    name = config.getString("name"),
    version = config.getString("version"),
    main = getString(config, "main"),
    authors = getStringList(config, "authors"),
    website = getString(config, "website"),
    depends = getStringList(config, "depends"),
    file
  )

  /**
    * Retrieve a string if available.
    *
    * @param config configuration.
    * @param key    key to access field.
    * @return optional string.
    */
  private def getString(config: Config, key: String): Option[String] = {
    if (config.hasPath(key)) {
      Some(config.getString(key))
    } else {
      None
    }
  }

  /**
    * Retrieve a list of string if available.
    *
    * @param config configuration.
    * @param key    key to access field.
    * @return optional seq string.
    */
  private def getStringList(config: Config, key: String): Seq[String] = {
    if (config.hasPath(key)) {
      config.getStringList(key).asScala
    } else {
      Seq.empty
    }
  }

}
