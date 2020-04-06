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
package io.techcode.streamy.event

import akka.NotUsed
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json.{JsNull, Json}

/**
  * Basic streamy event.
  *
  * @param payload json payload.
  * @param context optional context.
  * @tparam T type of context.
  */
case class StreamEvent[T] private(
  payload: Json,
  context: T
) {

  /**
    * Create a new stream event based on the current and mutate payload with `newPayload`
    *
    * @param newPayload payload replacement.
    * @return new stream event.
    */
  def mutate(newPayload: Json): StreamEvent[T] = StreamEvent(newPayload, context)

  /**
    * Create a new stream event based on the current and mutate context with `newContext`
    *
    * @param newContext context replacement.
    * @tparam Out type of the new context.
    * @return new stream event.
    */
  def mutate[Out](newContext: Out): StreamEvent[Out] = mutate[Out](payload, newContext)

  /**
    * Create a new stream event and mutate context and payload.
    *
    * @param newPayload payload replacement.
    * @param newContext context replacement.
    * @tparam Out type of the new context.
    * @return new stream event.
    */
  def mutate[Out](newPayload: Json, newContext: Out): StreamEvent[Out] = StreamEvent(newPayload, newContext)

  /**
    * Discard this stream event.
    *
    * @param msg message cause.
    * @tparam U type of the method.
    * @return nothing.
    */
  def discard[U](msg: String): U = throw new StreamException[T](this, msg)

  /**
    * Discard this stream event.
    *
    * @param cause exception cause.
    * @tparam U type of the method.
    * @return nothing.
    */
  def discard[U](cause: Throwable): U = throw new StreamException[T](this, cause)

}

/**
  * Stream event companion.
  */
object StreamEvent {

  // Empty stream event
  val Empty: StreamEvent[NotUsed] = StreamEvent[NotUsed](JsNull, NotUsed)

  /**
    * Create an stream event only using payload.
    *
    * @param payload payload to wrap.
    * @return new stream event.
    */
  def from(payload: Json): StreamEvent[NotUsed] = StreamEvent[NotUsed](payload, NotUsed)

}
