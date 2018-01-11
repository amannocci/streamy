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

import akka.util.ByteString
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.printer.JsonPrinter

import scala.language.postfixOps

/**
  * Sink transformer abstract implementation that provide
  * a convenient way to process a convertion from [[Json]] to [[ByteString]].
  */
abstract class SinkTransformer extends Transformer[Json, ByteString] {

  /**
    * Apply transform component on packet.
    *
    * @param pkt packet involved.
    * @return printing result.
    */
  def apply(pkt: Json): ByteString = {
    val printer: JsonPrinter = newPrinter(pkt)
    printer.print() match {
      case Some(result) => result
      case None => throw new StreamException(printer.error(), Some(pkt))
    }
  }

  /**
    * Create a new json printer.
    *
    * @param pkt packet involved.
    * @return json printer.
    */
  def newPrinter(pkt: Json): JsonPrinter

}
