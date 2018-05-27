/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2018
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

import akka.actor.{Actor, ActorLogging, DeadLetter}
import akka.event.LoggingAdapter
import io.techcode.streamy.event.ActorListener
import io.techcode.streamy.util.logging._

/**
  * Dead letter monitoring.
  */
class DeadLetterMonitor extends Actor with ActorLogging with ActorListener {

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[DeadLetter])
  }

  /**
    * Common mdc mapping.
    *
    * @param log implicit logging.
    */
  private def mdc(log: LoggingAdapter): LoggingAdapter = {
    log.putMDC("type", "monitor")
    log.putMDC("name", "dead-letter")
    log
  }

  override def receive: Receive = {
    case dead: DeadLetter => log.withContext {
      mdc(log).putMDC("sender", dead.sender.toString())
      log.putMDC("recipent", dead.recipient.toString())
      log.error(dead.message.toString)
    }
    case _ => log.withContext {
      mdc(log).warning("Dead letter monitor don't support other messages")
    }
  }

}
