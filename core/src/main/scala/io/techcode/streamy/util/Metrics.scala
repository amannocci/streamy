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
package io.techcode.streamy.util

import java.lang.management.ManagementFactory

import akka.actor.ActorSystem
import com.codahale.metrics._
import com.codahale.metrics.jvm._
import com.typesafe.config.Config
import io.circe._
import io.techcode.streamy.util.DurationUtil._
import io.techcode.streamy.util.json._
import org.slf4j.Logger

import scala.collection.mutable

/**
  * Provide some system metrics.
  */
object Metrics {

  // Metrics registry
  private val Registry = new MetricRegistry()

  // Metrics to collect
  Registry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer))
  Registry.register("jvm.gc", new GarbageCollectorMetricSet())
  Registry.register("jvm.memory", new MemoryUsageGaugeSet())
  Registry.register("jvm.threads", new ThreadStatesGaugeSet())

  /**
    * Create a new json reporter.
    *
    * @param actorSystem actor system involved.
    * @param log         logger output.
    * @param conf        reporter configuration.
    * @return a new json reporter.
    */
  def reporter(actorSystem: ActorSystem, log: Logger, conf: Config): Reporter = new JsonReporter(actorSystem, Registry, log, conf)

  class JsonReporter(system: ActorSystem, registry: MetricRegistry, log: Logger, conf: Config) extends Reporter {

    import system.dispatcher

    // Schedule report
    system.scheduler.schedule(conf.getDuration(ConfigConstants.MetricInitialDelay), conf.getDuration(ConfigConstants.MetricInterval), () => {
      // Create a new entry
      val entry: mutable.Map[String, Any] = new mutable.LinkedHashMap[String, Any]

      // Add all gauges (we have actually only gauges)
      Registry.getGauges.forEach((key, value) => entry.put(key, value.getValue))

      // Log
      log.info(Json.obj("type" -> "metrics").deepMerge(JsonUtil.fromMap(entry)))
    })
  }

}
