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
package io.techcode.streamy.component

import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.component.Transformer.{Config, ErrorBehaviour}
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._

/**
  * Abstract transformer implementation that provide a good way to handle errors.
  */
abstract class Transformer[In, Out](config: Config = Transformer.DefaultConfig) extends (In => Out) {

  /**
    * Handle parsing error by discarding or wrapping or skipping.
    *
    * @param state value of field when error is raised.
    * @param ex    exception if one is raised.
    * @return result json value.
    */
  def onError[T <: Json](msg: String = Transformer.GenericErrorMsg, state: T, ex: Option[Throwable] = None): T = {
    config.onError match {
      case ErrorBehaviour.Discard =>
        throw new StreamException(msg, state = Some(state))
      case ErrorBehaviour.DiscardAndReport =>
        throw new StreamException(msg, state = Some(state), ex)
      case ErrorBehaviour.Skip => state
    }
  }

}

/**
  * Transformer companion.
  */
object Transformer {

  // Generic error message
  val GenericErrorMsg = "Transformer failed to apply transformation"

  // Default configuration
  val DefaultConfig = new Config()

  // Component configuration
  class Config(
    val onError: ErrorBehaviour = ErrorBehaviour.DiscardAndReport
  )

  // Behaviour on error
  object ErrorBehaviour extends Enumeration {
    type ErrorBehaviour = Value
    val Discard, DiscardAndReport, Skip = Value
  }

}
