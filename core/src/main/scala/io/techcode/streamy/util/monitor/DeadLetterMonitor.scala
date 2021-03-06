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

import akka.actor.{Actor, DeadLetter, DiagnosticActorLogging}
import akka.event.Logging.MDC
import io.techcode.streamy.event.ActorListener

/**
  * Dead letter monitoring.
  */
class DeadLetterMonitor extends Actor with DiagnosticActorLogging with ActorListener {

  // Common mdc
  private val commonMdc = Map(
    "type" -> "monitor",
    "monitor" -> "dead-letter"
  )

  override def mdc(currentMessage: Any): MDC = currentMessage match {
    case evt: DeadLetter => commonMdc ++ Map(
      "sender" -> evt.sender.toString(),
      "recipent" -> evt.recipient.toString()
    )
    case _ => commonMdc
  }

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[DeadLetter])
  }

  override def receive: Receive = {
    case dead: DeadLetter =>
      log.error(dead.message.toString)
    case _ =>
      log.warning("Dead letter monitor don't support other messages")
  }

}
