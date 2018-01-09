/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018
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
package io.techcode.streamy.metric

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.config.Config
import io.techcode.streamy.metric.component.source.MetricSource
import io.techcode.streamy.plugin.{Plugin, PluginDescription}
import io.techcode.streamy.metric.util.ConfigConstants

import scala.reflect.io.Directory

/**
  * Metric plugin implementation.
  */
class MetricPlugin(
  system: ActorSystem,
  materializer: Materializer,
  description: PluginDescription,
  conf: Config,
  folder: Directory
) extends Plugin(system, materializer, description, conf, folder) {

  override def onStart(): Unit = {
    MetricSource.register(system, conf)

    if (conf.getBoolean(ConfigConstants.JvmEmbedded)) {
      MetricSource.jvm().runForeach(log.info(_))
    }
  }

  override def onStop(): Unit = ()

}
