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

import java.lang.reflect.Method

import akka.actor.{Actor, DiagnosticActorLogging, Timers}
import io.techcode.streamy.config.StreamyConfig
import io.techcode.streamy.event.{ActorListener, MonitorEvent}
import io.techcode.streamy.util.lang.SystemAccess
import io.techcode.streamy.util.monitor.ProcessMonitor.Tick

/**
  * Process monitor.
  */
class ProcessMonitor(conf: StreamyConfig.ProcessMonitor) extends Actor with DiagnosticActorLogging with ActorListener with Timers {

  override def preStart(): Unit = {
    timers.startTimerWithFixedDelay(ProcessMonitor.TickKey, ProcessMonitor.Tick, conf.refreshInterval)
  }

  override def receive: Receive = {
    case Tick => eventStream.publish(MonitorEvent.Process(
      timestamp = System.currentTimeMillis(),
      openFileDescriptors = ProcessMonitor.getOpenFileDescriptorCount,
      maxFileDescriptors = ProcessMonitor.getMaxFileDescriptorCount,
      cpuPercent = ProcessMonitor.getProcessCpuPercent,
      cpuTotal = ProcessMonitor.getProcessCpuTotalTime,
      memTotalVirtual = ProcessMonitor.getTotalVirtualMemorySize
    ))
  }

}

/**
  * Process monitor companion.
  */
object ProcessMonitor {

  // Tick implementation
  private case object TickKey

  private case object Tick

  // Method reflection
  val GetMaxFileDescriptorCountField: Option[Method] = SystemAccess.getUnixMethod("getMaxFileDescriptorCount")
  val GetOpenFileDescriptorCountField: Option[Method] = SystemAccess.getUnixMethod("getOpenFileDescriptorCount")
  val GetProcessCpuLoad: Option[Method] = SystemAccess.getMethod("getProcessCpuLoad")
  val GetProcessCpuTime: Option[Method] = SystemAccess.getMethod("getProcessCpuTime")
  val GetCommittedVirtualMemorySize: Option[Method] = SystemAccess.getMethod("getCommittedVirtualMemorySize")

  /**
    * Returns the number of opened file descriptors associated with the current process, or -1 if not supported.
    */
  def getOpenFileDescriptorCount: Long = ProcessMonitor.GetOpenFileDescriptorCountField
    .map[Long](_.invoke(SystemAccess.OsBean).asInstanceOf[Long])
    .getOrElse(-1)


  /**
    * Returns the maximum number of file descriptors allowed on the system, or -1 if not supported.
    */
  def getMaxFileDescriptorCount: Long = ProcessMonitor.GetMaxFileDescriptorCountField
    .map[Long](_.invoke(SystemAccess.OsBean).asInstanceOf[Long])
    .getOrElse(-1)

  /**
    * Returns the CPU time (in milliseconds) used by the process on which the Java virtual machine is running,
    * or -1 if not supported.
    */
  def getProcessCpuPercent: Short = ProcessMonitor.GetProcessCpuLoad
    .map[Double](_.invoke(SystemAccess.OsBean).asInstanceOf[Double])
    .filter(_ >= 0)
    .map[Short](value => (value * 100).asInstanceOf[Short])
    .getOrElse(-1)

  /**
    * Returns the CPU time (in milliseconds) used by the process on which the Java virtual machine is running,
    * or -1 if not supported.
    */
  def getProcessCpuTotalTime: Long = ProcessMonitor.GetProcessCpuTime
    .map[Long](_.invoke(SystemAccess.OsBean).asInstanceOf[Long])
    .getOrElse(-1)

  /**
    * Returns the size (in bytes) of virtual memory that is guaranteed to be available to the running process.
    */
  def getTotalVirtualMemorySize: Long = ProcessMonitor.GetCommittedVirtualMemorySize
    .map[Long](_.invoke(SystemAccess.OsBean).asInstanceOf[Long])
    .filter(_ >= 0)
    .getOrElse(-1)

}
