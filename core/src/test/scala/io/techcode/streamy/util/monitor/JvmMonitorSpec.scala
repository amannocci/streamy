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

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Jvm monitoring spec.
  */
class JvmMonitorSpec extends StreamyTestSystem {

  "Jvm monitoring" can {
    "be started and stopped" in {
      val jvmMonitor = system.actorOf(Props(classOf[JvmMonitor], StreamyConfig.JvmMonitor(
        enabled = true,
        10 minutes
      )))
      val probe = TestProbe()
      probe watch jvmMonitor
      jvmMonitor ! PoisonPill
      probe.expectTerminated(jvmMonitor)
    }

    "monitor correctly process" in {
      system.eventStream.subscribe(testActor, classOf[MonitorEvent.Jvm])
      system.actorOf(Props(classOf[JvmMonitor], StreamyConfig.JvmMonitor(
        enabled = true,
        0 seconds
      )))
      expectMsgClass(classOf[MonitorEvent.Jvm])
    }
  }

}
