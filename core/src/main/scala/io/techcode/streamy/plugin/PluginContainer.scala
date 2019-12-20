/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2019
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

import akka.actor.ActorRef
import com.typesafe.config.Config
import io.techcode.streamy.plugin.PluginState.PluginState

import scala.reflect.io.Directory

/**
  * Plugin data informations for loose coupling.
  *
  * @param description plugin description.
  * @param conf        plugin configuration.
  * @param dataFolder  plugin data folder.
  */
case class PluginData(
  description: PluginDescription,
  conf: Config,
  dataFolder: Directory
)

/**
  * Plugin container for loose coupling.
  *
  * @param ref         plugin actor ref.
  * @param description plugin description.
  * @param conf        plugin configuration.
  */
case class PluginContainer(
  ref: ActorRef = ActorRef.noSender,
  description: PluginDescription,
  conf: Config,
  state: PluginState = PluginState.Unknown
)

