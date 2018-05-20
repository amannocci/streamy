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

import java.lang.management.ManagementFactory

import akka.stream.Materializer
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.jvm.{BufferPoolMetricSet, GarbageCollectorMetricSet, MemoryUsageGaugeSet, ThreadStatesGaugeSet}
import io.techcode.streamy.metric.event.MetricEvent
import io.techcode.streamy.plugin.{Plugin, PluginData}
import io.techcode.streamy.util.json.JsonUtil
import pureconfig._

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

/**
  * Metric plugin implementation.
  */
class MetricPlugin(
  data: PluginData
) extends Plugin(data) {

  // Retrieve configuration
  private val conf: Config = loadConfigOrThrow[Config](data.conf)

  // Metrics registry
  private val Registry = new MetricRegistry()

  override def onStart(): Unit = {
    import system.dispatcher

    if (conf.jvm.isDefined) {
      // Metrics to collect
      Registry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer))
      Registry.register("jvm.gc", new GarbageCollectorMetricSet())
      Registry.register("jvm.memory", new MemoryUsageGaugeSet())
      Registry.register("jvm.threads", new ThreadStatesGaugeSet())

      // Scheduling
      val jvmConf = conf.jvm.get
      system.scheduler.schedule(jvmConf.initialDelay, jvmConf.interval) {
        // Create a new entry
        val entry: mutable.Map[String, Any] = mutable.AnyRefMap[String, Any]()

        // Add all gauges (we have actually only gauges)
        Registry.getGauges.forEach((key, value) => entry.put(key, value.getValue))

        // Emit event
        val evt = JsonUtil.fromRawMap(entry)
        system.eventStream.publish(MetricEvent.Jvm(evt))

        // If embedded log
        if (jvmConf.embedded) {
          log.info(evt)
        }
      }
    }
  }

  override def onStop(): Unit = ()

  // Plugin configuration
  private case class Config(
    jvm: Option[JvmConfig]
  )

  // Jvm plugin configuration
  private case class JvmConfig(
    initialDelay: FiniteDuration,
    interval: FiniteDuration,
    embedded: Boolean = true
  )

}
