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
import play.api.libs.json.{JsObject, JsString}

import scala.collection.mutable

/**
  * Syslog input implementation.
  */
class SyslogInput(spec: Map[String, String]) extends Input[JsObject] {

  // ByteBuf processor
  val FindInf = new ByteBufProcessor {
    override def process(value: Byte): Boolean = value != '<'
  }
  val FindSup = new ByteBufProcessor {
    override def process(value: Byte): Boolean = value != '>'
  }

  // Set all pref
  val facility: Option[String] = spec.get(SyslogInput.FacilityId)
  val timestamp: Option[String] = spec.get(SyslogInput.TimestampId)
  val hostname: Option[String] = spec.get(SyslogInput.HostnameId)
  val app: Option[String] = spec.get(SyslogInput.AppId)
  val proc: Option[String] = spec.get(SyslogInput.ProcId)
  val msgId: Option[String] = spec.get(SyslogInput.MsgId)
  val message: Option[String] = spec.get(SyslogInput.MessageId)

  override def apply(pkt: ByteString): JsObject = {
    // Grab new buffer
    val buf: ByteBuf = new ByteBuf(pkt)

    // Populate
    val mapping: mutable.Map[String, String] = new mutable.LinkedHashMap[String, String]

    // Read PRIVAL
    expect(buf, '<')
    capture(buf, facility, FindSup, mapping)

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
    capture(buf, proc, FindSpace, mapping)

    // Read message
    if (message.isDefined) {
      mapping.put(message.get, buf.slice().utf8String.trim)
    }

    // Split header and message
    JsObject(mapping.mapValues(JsString))
  }

  private def capture(buf: ByteBuf, ref: Option[String], processor: ByteBufProcessor, mapping: mutable.Map[String, String]): Unit = {
    if (ref.isDefined) {
      mapping.put(ref.get, buf.readBytes(processor).utf8String)
    } else {
      buf.skipBytes(processor)
    }
  }

  private def expect(buf: ByteBuf, ch: Char): Unit = {
    if (buf.readByte != ch) {
      throw new FramingException(s"Expected $ch at index ${buf.readerIndex}")
    }
  }

}

object SyslogInput {
  val FacilityId = "facility"
  val TimestampId = "timestamp"
  val HostnameId = "hostname"
  val AppId = "app"
  val ProcId = "proc"
  val MsgId = "msgId"
  val MessageId = "message"
}
