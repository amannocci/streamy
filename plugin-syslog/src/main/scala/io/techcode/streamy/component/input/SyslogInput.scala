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
package io.techcode.streamy.component.input

import akka.stream.scaladsl.Framing.FramingException
import akka.util.ByteString
import io.techcode.streamy.buffer.ByteBufProcessor._
import io.techcode.streamy.buffer.{ByteBuf, ByteBufProcessor}
import io.techcode.streamy.component.Input
import play.api.libs.json.{JsObject, JsString}

import scala.collection.mutable


/**
  * Syslog RFC5424 input implementation.
  */
private[input] class SyslogRFC5424Input(spec: Map[String, String]) extends Input[JsObject] {

  // Set all pref
  val facility: Option[String] = spec.get(SyslogInput.Id.Facility)
  val timestamp: Option[String] = spec.get(SyslogInput.Id.Timestamp)
  val hostname: Option[String] = spec.get(SyslogInput.Id.Hostname)
  val app: Option[String] = spec.get(SyslogInput.Id.App)
  val proc: Option[String] = spec.get(SyslogInput.Id.Proc)
  val msgId: Option[String] = spec.get(SyslogInput.Id.Msg)
  val structDataId: Option[String] = spec.get(SyslogInput.Id.StructData)
  val message: Option[String] = spec.get(SyslogInput.Id.Message)

  override def apply(pkt: ByteString): JsObject = {
    // Grab new buffer
    val buf: ByteBuf = new ByteBuf(pkt)

    // Populate
    val mapping: mutable.Map[String, String] = new mutable.LinkedHashMap[String, String]

    // Read PRIVAL
    expect(buf, '<')
    capture(buf, facility, FindCloseQuote, mapping)

    // Read version
    expect(buf, '1')
    expect(buf, ' ')

    // Read timestamp
    capture(buf, timestamp, FindSpace, mapping)

    // Read hostname
    capture(buf, hostname, FindSpace, mapping)

    // Read app name
    capture(buf, app, FindSpace, mapping)

    // Read proc id
    capture(buf, proc, FindSpace, mapping)

    // Read message id
    capture(buf, msgId, FindSpace, mapping)

    // Read structured data
    captureStructData(buf, structDataId, mapping)

    // Read message
    if (message.isDefined) {
      mapping.put(message.get, buf.slice().utf8String.trim)
    }

    // Split header and message
    JsObject(mapping.mapValues(JsString))
  }

  private def capture(buf: ByteBuf, ref: Option[String], processor: ByteBufProcessor, mapping: mutable.Map[String, String]): Unit = {
    if (ref.isDefined && buf.getByte != '-') {
      mapping.put(ref.get, buf.readString(processor))
    } else {
      buf.skipBytes(processor)
    }
  }

  private def captureStructData(buf: ByteBuf, ref: Option[String], mapping: mutable.Map[String, String]): Unit = {
    if (ref.isDefined && buf.getByte != '-') {
      buf.skipBytes(FindOpenBracket)
      mapping.put(ref.get, buf.readString(FindCloseBracket))
    } else {
      if (buf.getByte != '-') {
        buf.skipBytes(FindCloseBracket)
      }
      buf.skipBytes(FindSpace)
    }
  }

  private def expect(buf: ByteBuf, ch: Char): Unit = {
    if (buf.readByte != ch) {
      throw new FramingException(s"Expected $ch at index ${buf.readerIndex}")
    }
  }

}

/**
  * Syslog input companion.
  */
object SyslogInput {

  object Id {
    val Facility = "facility"
    val Timestamp = "timestamp"
    val Hostname = "hostname"
    val App = "app"
    val Proc = "proc"
    val Msg = "msgId"
    val StructData = "structDataId"
    val Message = "message"
  }

  /**
    * Create a syslog input RCF5424 compilant.
    *
    * @param spec enable features.
    * @return syslog input RCF5424 compilant.
    */
  def createRFC5424(spec: Map[String, String]): Input[JsObject] = new SyslogRFC5424Input(spec)

}
