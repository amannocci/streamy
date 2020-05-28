/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2020
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
package io.techcode.streamy.util.monitor

import java.lang.management.{BufferPoolMXBean, ManagementFactory}

import akka.actor.{Actor, Timers}
import io.techcode.streamy.config.StreamyConfig
import io.techcode.streamy.event.{ActorListener, MonitorEvent}
import io.techcode.streamy.util.lang.SystemAccess
import io.techcode.streamy.util.monitor.JvmMonitor.Tick

import scala.jdk.CollectionConverters._

/**
  * JVM monitor.
  */
class JvmMonitor(conf: StreamyConfig.JvmMonitor) extends Actor with ActorListener with Timers {

  override def preStart(): Unit = {
    timers.startPeriodicTimer(JvmMonitor.TickKey, JvmMonitor.Tick, conf.refreshInterval)
  }

  override def receive: Receive = {
    case Tick => eventStream.publish(MonitorEvent.Jvm(
      timestamp = System.currentTimeMillis(),
      uptime = SystemAccess.RuntimeBean.getUptime,
      memHeapCommitted = JvmMonitor.getHeapCommitted,
      memHeapUsed = JvmMonitor.getHeapUsed,
      memHeapMax = JvmMonitor.getHeapMax,
      memNonHeapCommitted = JvmMonitor.getNonHeapCommitted,
      memNonHeapUsed = JvmMonitor.getNonHeapUsed,
      thread = SystemAccess.ThreadBean.getThreadCount,
      threadPeak = SystemAccess.ThreadBean.getPeakThreadCount,
      classLoaded = SystemAccess.ClassLoadingBean.getLoadedClassCount,
      classUnloaded = SystemAccess.ClassLoadingBean.getUnloadedClassCount,
      classLoadedTotal = SystemAccess.ClassLoadingBean.getTotalLoadedClassCount,
      bufferPools = JvmMonitor.getBufferPools,
      garbageCollectors = JvmMonitor.getGarbageCollectors
    ))
  }

}

/**
  * Jvm monitor companion.
  */
object JvmMonitor {

  // Tick implementation
  private case object TickKey

  private case object Tick

  /**
    * Returns the amount of heap used in bytes.
    */
  private def getHeapUsed: Long = Math.max(0, SystemAccess.MemoryBean.getHeapMemoryUsage.getUsed)

  /**
    * Returns the amount of heap committed in bytes.
    */
  private def getHeapCommitted: Long = Math.max(0, SystemAccess.MemoryBean.getHeapMemoryUsage.getCommitted)

  /**
    * Returns the amount of heap max in bytes.
    */
  private def getHeapMax: Long = Math.max(0, SystemAccess.MemoryBean.getHeapMemoryUsage.getMax)

  /**
    * Returns the amount of non heap used in bytes.
    */
  private def getNonHeapUsed: Long = Math.max(0, SystemAccess.MemoryBean.getNonHeapMemoryUsage.getUsed)

  /**
    * Returns the amount of non heap committed in bytes.
    */
  private def getNonHeapCommitted: Long = Math.max(0, SystemAccess.MemoryBean.getNonHeapMemoryUsage.getCommitted)

  /**
    * Returns a list of buffer pools.
    */
  private def getBufferPools: Seq[MonitorEvent.Jvm.BufferPool] =
    ManagementFactory.getPlatformMXBeans(classOf[BufferPoolMXBean]).asScala.map { pool =>
      MonitorEvent.Jvm.BufferPool(
        name = pool.getName,
        count = pool.getCount,
        memUsed = pool.getMemoryUsed,
        totalCapacity = pool.getTotalCapacity
      )
    }.toSeq

  /**
    * Returns a list of garbage collectors.
    */
  private def getGarbageCollectors: Seq[MonitorEvent.Jvm.GarbageCollector] =
    SystemAccess.GarbageCollectorsBean.asScala.map { gc =>
      MonitorEvent.Jvm.GarbageCollector(
        name = gc.getName,
        collectionCount = gc.getCollectionCount,
        collectionTime = gc.getCollectionTime
      )
    }.toSeq

}
