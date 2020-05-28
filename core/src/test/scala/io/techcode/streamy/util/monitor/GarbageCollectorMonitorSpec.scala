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

import akka.actor.{PoisonPill, Props}
import akka.testkit.TestProbe
import io.techcode.streamy.StreamyTestSystem
import io.techcode.streamy.config.StreamyConfig
import io.techcode.streamy.event.MonitorEvent
import io.techcode.streamy.event.MonitorEvent.Jvm.{BufferPool, GarbageCollector}
import io.techcode.streamy.util.monitor.GarbageCollectorMonitorSpec.GarbageCollectorMonitorImpl

import scala.concurrent.duration._

/**
  * Garbage collector monitoring spec.
  */
class GarbageCollectorMonitorSpec extends StreamyTestSystem {

  "Garbage collector monitoring" can {
    "be started and stopped" in {
      val garbageCollectorMonitor = system.actorOf(Props(classOf[GarbageCollectorMonitor], StreamyConfig.GarbageCollectorMonitor(
        enabled = true,
        0.second,
        50, 25, 10
      )))
      val probe = TestProbe()
      probe watch garbageCollectorMonitor
      garbageCollectorMonitor ! PoisonPill
      probe.expectTerminated(garbageCollectorMonitor)
    }

    "monitor correctly process" in {
      system.eventStream.subscribe(testActor, classOf[MonitorEvent.GarbageCollectorOverhead])
      val garbageCollectorMonitor = system.actorOf(Props(classOf[GarbageCollectorMonitorImpl], StreamyConfig.GarbageCollectorMonitor(
        enabled = true,
        0.second,
        50, 25, 10
      )))
      garbageCollectorMonitor ! GarbageCollectorMonitorSpec.Heartbeat
      expectMsg(GarbageCollectorMonitorSpec.Heartbeat)

      system.eventStream.publish(MonitorEvent.Jvm(
        timestamp = System.currentTimeMillis(),
        uptime = 986,
        memHeapUsed = 1126816,
        memHeapCommitted = 264241152,
        memHeapMax = 4188012544L,
        memNonHeapCommitted = 34865152,
        memNonHeapUsed = 30531400,
        thread = 11,
        threadPeak = 11,
        classLoaded = 4019,
        classLoadedTotal = 4019,
        classUnloaded = 0,
        bufferPools = Seq(
          BufferPool("mapped", 0, 0, 0),
          BufferPool("direct", 1, 8192, 8192)
        ),
        garbageCollectors = Seq(
          GarbageCollector("G1 Young Generation", 2, 11),
          GarbageCollector("G1 Old Generation", 0, 0)
        )
      ))
      system.eventStream.publish(MonitorEvent.Jvm(
        timestamp = System.currentTimeMillis() + 100,
        uptime = 986,
        memHeapUsed = 1126816,
        memHeapCommitted = 264241152,
        memHeapMax = 4188012544L,
        memNonHeapCommitted = 34865152,
        memNonHeapUsed = 30531400,
        thread = 11,
        threadPeak = 11,
        classLoaded = 4019,
        classLoadedTotal = 4019,
        classUnloaded = 0,
        bufferPools = Seq(
          BufferPool("mapped", 0, 0, 0),
          BufferPool("direct", 1, 8192, 8192)
        ),
        garbageCollectors = Seq(
          GarbageCollector("G1 Young Generation", 3, 12),
          GarbageCollector("G1 Old Generation", 0, 0)
        )
      ))
      expectMsgClass(classOf[MonitorEvent.GarbageCollectorOverhead])
    }
  }

}

/**
  * Garbage collector monitor spec.
  */
object GarbageCollectorMonitorSpec {

  case object Heartbeat

  // Implement heartbeat
  class GarbageCollectorMonitorImpl(
    conf: StreamyConfig.GarbageCollectorMonitor
  ) extends GarbageCollectorMonitor(conf) {

    override def receive: Receive = {
      case _: Heartbeat.type => sender() ! Heartbeat
      case v => super.receive(v)
    }

  }

}
