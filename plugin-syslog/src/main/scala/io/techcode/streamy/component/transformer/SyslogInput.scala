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
package io.techcode.streamy.component.transformer

import akka.util.ByteString
import io.techcode.streamy.buffer.ByteBufProcessor._
import io.techcode.streamy.buffer.{ByteBuf, ByteBufProcessor}
import io.techcode.streamy.component.Transformer
import io.techcode.streamy.component.transformer.SyslogInput.RFC5424Config
import io.techcode.streamy.util.json._


/**
  * Syslog RFC5424 input transformer implementation.
  */
private[transformer] class SyslogRFC5424Input(config: RFC5424Config) extends Transformer[ByteString, Json] {

  override def apply(pkt: ByteString): Json = {
    // Grab new buffer
    val buf: ByteBuf = new ByteBuf(pkt)

    // Populate
    val builder: JsObjectBuilder = Json.objectBuilder()

    // Read PRIVAL
    expect(buf, '<')
    capturePrival(builder, buf, config)

    // Read version
    expect(buf, '1')
    expect(buf, ' ')

    // Read timestamp
    captureString(builder, buf, config.timestamp, FindSpace)

    // Read hostname
    captureString(builder, buf, config.hostname, FindSpace)

    // Read app name
    captureString(builder, buf, config.app, FindSpace)

    // Read proc id
    captureString(builder, buf, config.proc, FindSpace)

    // Read message id
    captureString(builder, buf, config.msgId, FindSpace)

    // Read structured data
    captureStructData(builder, buf, config.structData)

    // Read message
    if (config.message.isDefined) {
      builder.put(config.message.get, buf.slice().utf8String.trim)
    }

    // Split header and message
    builder.result()
  }

  /**
    * Capture a syslog part.
    *
    * @param buf       current bytebuf
    * @param ref       reference part.
    * @param processor bytebuf processor used to delimite part.
    */
  private def captureString(builder: JsObjectBuilder, buf: ByteBuf, ref: Option[String], processor: ByteBufProcessor): Unit = {
    if (ref.isDefined && buf.getByte != '-') {
      builder.put(ref.get, buf.readString(processor))
    } else {
      buf.skipBytes(processor)
    }
  }

  /**
    * Capture a syslog part.
    *
    * @param buf  current bytebuf.
    * @param conf reference part.
    */
  private def capturePrival(builder: JsObjectBuilder, buf: ByteBuf, conf: RFC5424Config): Unit = {
    if (config.facility.isDefined || config.severity.isDefined) {
      // Read prival in tmp
      val prival = buf.readDigit(FindCloseQuote)

      // Read severity or facility
      if (config.facility.isDefined) {
        builder.put(config.facility.get, prival >> 3)
      }
      if (config.severity.isDefined) {
        builder.put(config.severity.get, prival & 7)
      }
    } else {
      buf.skipBytes(FindCloseQuote)
    }
  }

  /**
    * Capture a syslog part.
    *
    * @param buf current bytebuf.
    * @param ref reference part.
    */
  private def captureStructData(builder: JsObjectBuilder, buf: ByteBuf, ref: Option[String]): Unit = {
    if (ref.isDefined && buf.getByte != '-') {
      buf.skipBytes(FindOpenBracket)
      builder.put(ref.get, buf.readString(FindCloseBracket))
    } else {
      if (buf.getByte != '-') {
        buf.skipBytes(FindCloseBracket)
      }
      buf.skipBytes(FindSpace)
    }
  }

  /**
    * Except a given character.
    *
    * @param buf current bytebuf.
    * @param ch  excepted character.
    */
  private def expect(buf: ByteBuf, ch: Char): Unit = {
    if (buf.readByte != ch) {
      onError(s"Expected $ch at index ${buf.readerIndex}", JsString(buf.toString))
    }
  }

}

/**
  * Syslog input companion.
  */
object SyslogInput {

  object Id {
    val Facility = "facility"
    val Severity = "severity"
    val Timestamp = "timestamp"
    val Hostname = "hostname"
    val App = "app"
    val Proc = "proc"
    val MsgId = "msgId"
    val StructData = "structDataId"
    val Message = "message"
  }

  // Component configuration
  case class RFC5424Config(
    facility: Option[String] = None,
    severity: Option[String] = None,
    timestamp: Option[String] = None,
    hostname: Option[String] = None,
    app: Option[String] = None,
    proc: Option[String] = None,
    msgId: Option[String] = None,
    structData: Option[String] = None,
    message: Option[String] = None
  )

}
