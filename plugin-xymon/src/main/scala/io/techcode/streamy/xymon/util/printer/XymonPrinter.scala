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
package io.techcode.streamy.xymon.util.printer

import akka.util.ByteString
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.printer.{ByteStringPrinter, DerivedByteStringPrinter}
import io.techcode.streamy.xymon.component.XymonTransformer


/**
  * Xymon printer companion
  */
object XymonPrinter {

  val Host: String = "www,example,com"
  val Service: String = "streamy"
  val Color: String = "red"
  val Empty: String = ""
  val Space: Char = ' '
  val Plus: Char = '+'
  val Slash: Char = '/'
  val Colon: Char = ':'
  val Dot: Char = '.'

  /**
    * Create a xymon printer that transform incoming [[Json]] to [[ByteString]].
    *
    * @param conf printer configuration.
    * @return new xymon printer.
    */
  def printer(conf: XymonTransformer.Printer.Config): ByteStringPrinter[Json] = new XymonPrinter(conf)

}

/**
  * Xymon printer that transforms incoming [[Json]] to [[ByteString]]
  */
private class XymonPrinter(conf: XymonTransformer.Printer.Config) extends PrinterHelpers {

  // Fast binding access
  private val binding: XymonTransformer.Printer.Binding = conf.binding

  override def run(): ByteString = {
    // Add status
    builder.append(XymonTransformer.Id.Status)

    // Optionally add lifetime
    computeValHook(binding.lifetimePointer, XymonPrinter.Empty) {
      builder.append(XymonPrinter.Plus)
    }

    // Optionally add group
    computeValHook(binding.groupPointer, XymonPrinter.Empty) {
      builder.append(XymonPrinter.Slash)
      builder.append(XymonTransformer.Id.Group)
      builder.append(XymonPrinter.Colon)
    }

    // Space
    builder.append(XymonPrinter.Space)

    // Hostname and testname
    computeVal(binding.hostPointer, XymonPrinter.Host)
    builder.append(XymonPrinter.Dot)
    computeVal(binding.servicePointer, XymonPrinter.Service)

    // Space
    builder.append(XymonPrinter.Space)

    // Color
    computeVal(binding.colorPointer, XymonPrinter.Color)

    // Optionally add space and message
    computeValHook(binding.messagePointer, XymonPrinter.Empty) {
      builder.append(XymonPrinter.Space)
    }

    builder.toByteString
  }
}

private abstract class PrinterHelpers extends DerivedByteStringPrinter[Json] {

  /**
    * Print data part to format xymon message.
    *
    * @param conf         name of the field.
    * @param defaultValue default value.
    */
  def computeVal(conf: Option[JsonPointer], defaultValue: String): Unit =
    computeValHook(conf, defaultValue)((): Unit)

  /**
    * Print data part to format xymon message.
    *
    * @param conf         name of the field.
    * @param defaultValue default value.
    * @param hook         hook to trigger if a value is process.
    */
  def computeValHook(conf: Option[JsonPointer], defaultValue: String)(hook: => Unit): Unit = {
    if (conf.isDefined) {
      data.evaluate(conf.get).ifExists[String] { result =>
        hook
        builder.append(result)
      }
    } else {
      if (defaultValue.nonEmpty) {
        hook
        builder.append(defaultValue)
      }
    }
  }
}
