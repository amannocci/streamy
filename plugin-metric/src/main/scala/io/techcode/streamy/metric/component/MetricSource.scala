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
package io.techcode.streamy.metric.component

import java.lang.management.ManagementFactory

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.jvm.{BufferPoolMetricSet, GarbageCollectorMetricSet, MemoryUsageGaugeSet, ThreadStatesGaugeSet}
import com.typesafe.config.Config
import io.techcode.streamy.metric.util.ConfigConstants
import io.techcode.streamy.util.DurationUtil._
import io.techcode.streamy.util.json._

import scala.collection.mutable

object MetricSource {

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
  private var jvmSource: Source[Json, NotUsed] = Source.empty

  /**
    * Register metric source.
    *
    * @param actorSystem streamy actor system.
    * @param conf        streamy configuration.
    */
  private[streamy] def register(actorSystem: ActorSystem, conf: Config)(implicit materializer: Materializer): Unit = {
    jvmSource = Source.tick(conf.getDuration(ConfigConstants.JvmInitialDelay), conf.getDuration(ConfigConstants.JvmInterval), ())
      .map { _ =>
        // Create a new entry
        val entry: mutable.Map[String, Any] = mutable.AnyRefMap[String, Any]()

        // Add all gauges (we have actually only gauges)
        Registry.getGauges.forEach((key, value) => entry.put(key, value.getValue))

        // Log
        JsonUtil.fromRawMap(entry)
      }.toMat(BroadcastHub.sink(bufferSize = InMemoryMetric))(Keep.right).run()
  }

  /**
    * Create a jvm metric source that provide metrics.
    *
    * @return new jvm metric source.
    */
  def jvm(): Source[Json, NotUsed] = jvmSource

}
