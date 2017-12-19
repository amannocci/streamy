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
package io.techcode.streamy.util

import io.techcode.streamy.util.json._
import org.apache.commons.lang3.exception.ExceptionUtils

import scala.util.control.NoStackTrace

/**
  * A stream exception raised when an error occured in a stream.
  *
  * @param msg   error message.
  * @param state current pkt state.
  * @param ex    parent exception raised.
  */
class StreamException(msg: String, state: Option[Json] = None, ex: Option[Throwable] = None) extends RuntimeException(msg) with NoStackTrace {

  /**
    * Convert the exception to a json object.
    *
    * @return json object.
    */
  def toJson: Json = {
    var result: JsObject = Json.obj("message" -> msg)
    if (state.isDefined) {
      result = result.put("state", state.get)
    }
    if (ex.isDefined) {
      result = result.put("exception", ExceptionUtils.getStackTrace(ex.get))
    }
    result
  }

}
