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

import java.io.IOException
import java.lang.reflect.Method
import java.nio.file.{Files, Paths}

import akka.actor.{Actor, DiagnosticActorLogging, Timers}
import io.techcode.streamy.config.StreamyConfig
import io.techcode.streamy.event.{ActorListener, MonitorEvent}
import io.techcode.streamy.util.lang.SystemAccess
import io.techcode.streamy.util.monitor.OsMonitor.Tick

/**
  * OS monitor.
  */
class OsMonitor(conf: StreamyConfig.OsMonitor) extends Actor with DiagnosticActorLogging with ActorListener with Timers {

  override def preStart(): Unit = {
    timers.startPeriodicTimer(OsMonitor.TickKey, OsMonitor.Tick, conf.refreshInterval)
  }

  override def receive: Receive = {
    case Tick => eventStream.publish(MonitorEvent.Os(
      timestamp = System.currentTimeMillis(),
      cpuPercent = OsMonitor.getSystemCpuPercent,
      cpuLoadAverage = OsMonitor.getSystemLoadAverage,
      memFree = OsMonitor.getFreePhysicalMemorySize,
      memAvailable = OsMonitor.getAvailablePhysicalMemorySize,
      memTotal = OsMonitor.getTotalPhysicalMemorySize,
      swapFree = OsMonitor.getFreeSwapSpaceSize,
      swapTotal = OsMonitor.getTotalSwapSpaceSize
    ))
  }

}

/**
  * Os monitor companion.
  */
object OsMonitor {

  // Tick implementation
  private case object TickKey

  private case object Tick

  // Method reflection
  val GetFreePhysicalMemorySize: Option[Method] = SystemAccess.getMethod("getFreePhysicalMemorySize")
  val GetTotalPhysicalMemorySize: Option[Method] = SystemAccess.getMethod("getTotalPhysicalMemorySize")
  val GetFreeSwapSpaceSize: Option[Method] = SystemAccess.getMethod("getFreeSwapSpaceSize")
  val GetTotalSwapSpaceSize: Option[Method] = SystemAccess.getMethod("getTotalSwapSpaceSize")
  val GetSystemLoadAverage: Option[Method] = SystemAccess.getMethod("getSystemLoadAverage")
  val GetSystemCpuLoad: Option[Method] = SystemAccess.getMethod("getSystemCpuLoad")

  /**
    * Returns the CPU time (in milliseconds) used by the system on which the Java virtual machine is running,
    * or -1 if not supported.
    */
  def getSystemCpuPercent: Short = GetSystemCpuLoad
    .map[Double](_.invoke(SystemAccess.OsBean).asInstanceOf[Double])
    .filter(_ >= 0)
    .map[Short](value => (value * 100).asInstanceOf[Short])
    .getOrElse(-1)

  /**
    * The system load averages as an array.
    *
    * On Windows, this method returns empty array.
    * On Linux, this method returns the 1, 5, and 15-minute load averages.
    * On macOS, this method should return the 1-minute load average.
    *
    * @return the available system load averages.
    */
  def getSystemLoadAverage: Array[Double] = {
    if (SystemAccess.isWindows) {
      Array.emptyDoubleArray
    } else if (SystemAccess.isLinux) {
      try {
        val procLoadAvg = Files.readAllLines(Paths.get("/proc/loadavg")).get(0)
        if (procLoadAvg.matches("(\\d+\\.\\d+\\s+){3}\\d+/\\d+\\s+\\d+")) {
          val fields = procLoadAvg.split("\\s+")
          Array[Double](
            fields(0).toDouble,
            fields(1).toDouble,
            fields(2).toDouble
          )
        } else {
          Array.emptyDoubleArray
        }
      } catch {
        case _: IOException => Array.emptyDoubleArray
      }
    } else {
      Array[Double](
        OsMonitor.GetSystemLoadAverage
          .map[Double](_.invoke(SystemAccess.OsBean).asInstanceOf[Double])
          .filter(_ >= 0)
          .getOrElse(-1),
        -1,
        -1
      )
    }
  }

  /**
    * Returns the amount of available physical memory in bytes.
    */
  def getAvailablePhysicalMemorySize: Long = {
    if (SystemAccess.isWindows) {
      0
    } else {
      try {
        Files.readAllLines(Paths.get("/proc/meminfo"))
          .stream().filter(s => s.startsWith("MemAvailable:"))
          .findFirst()
          .map[Array[String]](mem => mem.split("\\s+"))
          .map[Long](split => split(1).toLong * 1024)
          .orElse(-1L)
      } catch {
        case _: IOException => -1
      }
    }
  }

  /**
    * Returns the amount of free physical memory in bytes.
    */
  def getFreePhysicalMemorySize: Long = OsMonitor.GetFreePhysicalMemorySize
    .map[Long](_.invoke(SystemAccess.OsBean).asInstanceOf[Long])
    .filter(_ >= 0)
    .getOrElse(-1)

  /**
    * Returns the total amount of physical memory in bytes.
    */
  def getTotalPhysicalMemorySize: Long = OsMonitor.GetTotalPhysicalMemorySize
    .map[Long](_.invoke(SystemAccess.OsBean).asInstanceOf[Long])
    .filter(_ >= 0)
    .getOrElse(-1)

  /**
    * Returns the amount of free swap space in bytes.
    */
  def getFreeSwapSpaceSize: Long = OsMonitor.GetFreeSwapSpaceSize
    .map[Long](_.invoke(SystemAccess.OsBean).asInstanceOf[Long])
    .filter(_ >= 0)
    .getOrElse(-1)

  /**
    * Returns the amount of free swap space in bytes.
    */
  def getTotalSwapSpaceSize: Long = OsMonitor.GetTotalSwapSpaceSize
    .map[Long](_.invoke(SystemAccess.OsBean).asInstanceOf[Long])
    .filter(_ >= 0)
    .getOrElse(-1)

}
