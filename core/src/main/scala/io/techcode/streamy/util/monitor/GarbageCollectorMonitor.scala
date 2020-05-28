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

import akka.actor.{Actor, DiagnosticActorLogging}
import io.techcode.streamy.config.StreamyConfig
import io.techcode.streamy.event.{ActorListener, MonitorEvent}

/**
  * Garbage collector monitor.
  */
class GarbageCollectorMonitor(
  conf: StreamyConfig.GarbageCollectorMonitor
) extends Actor with DiagnosticActorLogging with ActorListener {

  // Events
  private var previousEvent: Option[MonitorEvent.Jvm] = None
  private var currentEvent: Option[MonitorEvent.Jvm] = None

  // Overhead log message
  private val overheadLogMsg: String = "Spent [{}] garbage collecting in the last [{}] representing [{}%]"

  override def preStart(): Unit = {
    eventStream.subscribe(self, classOf[MonitorEvent.Jvm])
  }

  override def receive: Receive = {
    case evt: MonitorEvent.Jvm =>
      if (currentEvent.isDefined) {
        previousEvent = currentEvent
        currentEvent = Some(evt)
        checkOverhead()
      } else {
        currentEvent = Some(evt)
      }
  }

  /**
    * Check for garbage collector overhead.
    */
  private def checkOverhead(): Unit = {
    // Safe because this method is called only when a have two events
    val prevEvt = previousEvent.get
    val currEvt = currentEvent.get

    // Compute elapsed time
    val elapsed = currEvt.timestamp - prevEvt.timestamp
    var time: Long = 0
    for (i <- currEvt.garbageCollectors.indices) {
      time += currEvt.garbageCollectors(i).collectionTime - prevEvt.garbageCollectors(i).collectionTime
    }

    // Percent of overhead
    val percent = ((time / elapsed.toDouble) * 100).toShort
    if (percent >= conf.thresholdWarn) {
      log.warning(overheadLogMsg, time, elapsed, percent)
    } else if (percent >= conf.thresholdInfo) {
      log.info(overheadLogMsg, time, elapsed, percent)
    } else if (percent >= conf.thresholdDebug) {
      log.debug(overheadLogMsg, time, elapsed, percent)
    }

    // Publish event
    eventStream.publish(MonitorEvent.GarbageCollectorOverhead(
      System.currentTimeMillis(),
      time,
      elapsed,
      percent
    ))
  }

}
