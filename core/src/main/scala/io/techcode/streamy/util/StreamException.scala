/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2020
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
package io.techcode.streamy.util

import com.google.common.base.Objects
import io.techcode.streamy.event.StreamEvent

import scala.util.control.NoStackTrace

/**
  * A stream exception raised when an error occured in a stream.
  *
  * @param state current event state.
  * @param msg   message error cause.
  * @param cause exception cause.
  */
case class StreamException(
  state: StreamEvent,
  msg: String,
  cause: Throwable
) extends RuntimeException(msg, cause) with NoStackTrace {

  // Create using state and exception cause
  def this(state: StreamEvent, cause: Throwable) {
    this(state, StreamException.defaultMsg, cause)
  }

  // Create using state and message cause
  def this(state: StreamEvent, msg: String) {
    this(state, msg, null)
  }

  override def equals(that: Any): Boolean = that match {
    case that: StreamException => this.hashCode == that.hashCode
    case _ => false
  }

  override def hashCode: Int = Objects.hashCode(msg, state)

}

/**
  * Stream exception companion.
  */
object StreamException {

  private[StreamException] val defaultMsg: String = "A stream exception occured"

}
