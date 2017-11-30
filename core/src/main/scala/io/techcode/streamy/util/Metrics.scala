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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import com.codahale.metrics._
import com.codahale.metrics.jvm._
import com.typesafe.config.Config
import io.techcode.streamy.util.DurationUtil._
import io.techcode.streamy.util.json._

import scala.collection.mutable

/**
  * Provide some system metrics.
  */
object Metrics {

  // Metrics registry
  private val Registry = new MetricRegistry()

  // Number of metrics to keep in memory before drop
  private val InMemoryMetric = 64

  // Metrics to collect
  Registry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer))
  Registry.register("jvm.gc", new GarbageCollectorMetricSet())
  Registry.register("jvm.memory", new MemoryUsageGaugeSet())
  Registry.register("jvm.threads", new ThreadStatesGaugeSet())

  // Source
  private var metricSource: Source[Json, NotUsed] = Source.empty

  /**
    * Register metric source.
    *
    * @param actorSystem streamy actor system.
    * @param conf        streamy configuration.
    */
  private[streamy] def register(actorSystem: ActorSystem, conf: Config)(implicit materializer: Materializer): Unit = {
    metricSource = Source.tick(conf.getDuration(ConfigConstants.StreamyMetricInitialDelay), conf.getDuration(ConfigConstants.StreamyMetricInterval), ())
      .map { _ =>
        // Create a new entry
        val entry: mutable.Map[String, Any] = new mutable.LinkedHashMap[String, Any]

        // Add all gauges (we have actually only gauges)
        Registry.getGauges.forEach((key, value) => entry.put(key, value.getValue))

        // Log
        Json.obj("type" -> "metrics").deepMerge(JsonUtil.fromRawMap(entry)).get
      }.toMat(BroadcastHub.sink(bufferSize = InMemoryMetric))(Keep.right).run()
  }

  /**
    * Retrieve the metric json source.
    *
    * @return metric json source.
    */
  def source(): Source[Json, NotUsed] = metricSource

}
