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
package io.techcode.streamy.xymon.util.printer

import akka.util.ByteString
import io.techcode.streamy.util.Binder
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.printer.{ByteStringPrinter, DerivedByteStringPrinter}
import io.techcode.streamy.xymon.component.XymonTransformer


/**
  * Xymon printer companion
  */
object XymonPrinter {

  def printer(conf: XymonTransformer.Printer.Config): ByteStringPrinter = new XymonPrinter(conf)

  val Host: String = "www,example,com"
  val Service: String = "streamy"
  val Color: String = "red"
  val Empty: String = ""
  val Space: Char = ' '
  val Plus: Char = '+'
  val Slash: Char = '/'
  val Colon: Char = ':'
  val Dot: Char = '.'
}

/**
  * Xymon printer that transforms incoming [[Json]] to [[ByteString]]
  */
private class XymonPrinter(conf: XymonTransformer.Printer.Config) extends PrinterHelpers {

  private val binding: XymonTransformer.Printer.Binding = conf.binding

  override def run(): ByteString = {
    // status
    builder.append(XymonTransformer.Id.Status)

    // optionally add lifetime
    computeVal(binding.lifetime, XymonPrinter.Empty) {
      builder.append(XymonPrinter.Plus)
    }

    // optionally add group
    computeVal(binding.group, XymonPrinter.Empty) {
      builder.append(XymonPrinter.Slash)
      builder.append(XymonTransformer.Id.Group)
      builder.append(XymonPrinter.Colon)
    }

    // space
    builder.append(XymonPrinter.Space)

    // hostname and testname
    computeVal(binding.host, XymonPrinter.Host)()
    builder.append(XymonPrinter.Dot)
    computeVal(binding.service, XymonPrinter.Service)()

    // space
    builder.append(XymonPrinter.Space)

    // color
    computeVal(binding.color, XymonPrinter.Color)()

    // optionally add space and message
    computeVal(binding.message, XymonPrinter.Empty) {
      builder.append(XymonPrinter.Space)
    }

    ByteString(builder.toString)
  }
}

private abstract class PrinterHelpers extends DerivedByteStringPrinter {
  def computeVal(conf: Binder, defaultValue: String)(hook: => Unit): Unit = {
    if (conf.isDefined) {
      conf.bind(builder, data)(hook)
    } else {
      if (defaultValue.nonEmpty) {
        hook
        builder.append(defaultValue)
      }
    }
  }
}