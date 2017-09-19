/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017
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
package io.techcode.streamy.stream

import org.apache.commons.lang3.exception.ExceptionUtils
import play.api.libs.json._

import scala.util.control.NoStackTrace

/**
  * A stream exception raised when an error occured in a stream.
  *
  * @param msg   error message.
  * @param state current pkt state.
  * @param ex    parent exception raised.
  */
class StreamException(msg: String, state: Option[JsValue] = None, ex: Option[Throwable] = None) extends RuntimeException(msg) with NoStackTrace {

  /**
    * Convert the exception to a json object.
    *
    * @return json object.
    */
  def toJson: JsObject = {
    val stateMsg = state match {
      case Some(value: JsString) => value.value
      case _ => state.getOrElse[JsValue](StreamException.Empty).toString()
    }
    Json.obj(
      "message" -> msg,
      "exception" -> ExceptionUtils.getStackTrace(ex.getOrElse(this)),
      "state" -> stateMsg
    )
  }

}

/**
  * Stream exception companion.
  */
object StreamException {

  // Empty json object
  private val Empty: JsObject = Json.obj()

  /**
    * Create a stream exception based on Json error.
    *
    * @param msg reason message.
    * @param err json error involved.
    * @return stream exception.
    */
  def create(msg: String, err: JsError): StreamException = new StreamException(msg, state = Some(JsError.toJson(err)))

}
