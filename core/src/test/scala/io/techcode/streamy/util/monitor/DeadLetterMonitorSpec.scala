/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2019
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

import akka.actor.{DeadLetter, PoisonPill, Props}
import akka.testkit.TestProbe
import io.techcode.streamy.StreamyTestSystem

/**
  * Dead letter monitoring spec.
  */
class DeadLetterMonitorSpec extends StreamyTestSystem {

  "Dead letter monitoring" can {
    "be started and stopped" in {
      val deadLetterMonitor = system.actorOf(Props[DeadLetterMonitor])
      val probe = TestProbe()
      probe watch deadLetterMonitor
      deadLetterMonitor ! PoisonPill
      probe.expectTerminated(deadLetterMonitor)
    }

    "handle correctly dead letter" in {
      val deadLetterMonitor = system.actorOf(Props[DeadLetterMonitor])
      val probe = TestProbe()
      probe watch deadLetterMonitor
      system.eventStream.subscribe(deadLetterMonitor, classOf[DeadLetter])
      system.eventStream.publish(DeadLetter("Test", deadLetterMonitor, deadLetterMonitor))
      deadLetterMonitor ! PoisonPill
      probe.expectTerminated(deadLetterMonitor)
    }

    "not receive message by default" in {
      val deadLetterMonitor = system.actorOf(Props[DeadLetterMonitor])
      val probe = TestProbe()
      probe watch deadLetterMonitor
      deadLetterMonitor ! "test"
      deadLetterMonitor ! PoisonPill
      probe.expectTerminated(deadLetterMonitor)
    }
  }

}
