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
package io.techcode.streamy.event

import akka.actor.Actor
import akka.event.EventStream

/**
  * Scala API: Mix in ActorListener into your Actor to easily obtain a reference to the eventStream,
  * which is available under the name "eventStream".
  * Lifecyle hooks are override for easy usage.
  *
  * {{{
  * class MyActor extends Actor with ActorListener {
  *   override def preStart(): Unit = {
  *     eventStream.subscribe(self, classOf[MyEvent])
  *   }
  *
  *   def receive = {
  *     case _: MyEvent => log.info("We've got yet another event on our hands")
  *   }
  * }
  * }}}
  */
trait ActorListener {
  this: Actor ⇒

  val eventStream: EventStream = context.system.eventStream

  // Don't re-subscribe, skip call to preStart
  override def postRestart(reason: Throwable): Unit = ()

  // Don't remove subscription, skip call to postStop, no children to stop
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = ()

  override def postStop(): Unit = eventStream.unsubscribe(self)

}
