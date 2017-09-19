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

import akka.util.ByteString
import io.techcode.streamy.buffer.ByteBufProcessor._
import io.techcode.streamy.buffer.{ByteBuf, ByteBufProcessor}
import io.techcode.streamy.component.input.SyslogInput.RFC5424Config
import io.techcode.streamy.stream.StreamException
import io.techcode.streamy.util.JsonUtil
import play.api.libs.json.{JsObject, JsString, Json}

import scala.collection.mutable


/**
  * Syslog RFC5424 input implementation.
  */
private[input] class SyslogRFC5424Input(config: RFC5424Config) extends (ByteString => JsObject) {

  override def apply(pkt: ByteString): JsObject = {
    // Grab new buffer
    val buf: ByteBuf = new ByteBuf(pkt)

    // Populate
    implicit val mapping: mutable.Map[String, Any] = new mutable.LinkedHashMap[String, Any]

    // Read PRIVAL
    expect(buf, '<')
    capturePrival(buf, config)

    // Read version
    expect(buf, '1')
    expect(buf, ' ')

    // Read timestamp
    captureString(buf, config.timestamp, FindSpace)

    // Read hostname
    captureString(buf, config.hostname, FindSpace)

    // Read app name
    captureString(buf, config.app, FindSpace)

    // Read proc id
    captureString(buf, config.proc, FindSpace)

    // Read message id
    captureString(buf, config.msgId, FindSpace)

    // Read structured data
    captureStructData(buf, config.structData)

    // Read message
    if (config.message.isDefined) {
      mapping.put(config.message.get, buf.slice().utf8String.trim)
    }

    // Split header and message
    JsonUtil.toJson(mapping)
  }

  /**
    * Capture a syslog part.
    *
    * @param buf       current bytebuf
    * @param ref       reference part.
    * @param processor bytebuf processor used to delimite part.
    */
  private def captureString(buf: ByteBuf, ref: Option[String], processor: ByteBufProcessor)(implicit mapping: mutable.Map[String, Any]): Unit = {
    if (ref.isDefined && buf.getByte != '-') {
      mapping.put(ref.get, buf.readString(processor))
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
  private def capturePrival(buf: ByteBuf, conf: RFC5424Config)(implicit mapping: mutable.Map[String, Any]): Unit = {
    if (config.facility.isDefined || config.severity.isDefined) {
      // Read prival in tmp
      val prival = buf.readDigit(FindCloseQuote)

      // Read severity or facility
      if (config.facility.isDefined) {
        mapping.put(config.facility.get, prival >> 3)
      }
      if (config.severity.isDefined) {
        mapping.put(config.severity.get, prival & 7)
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
  private def captureStructData(buf: ByteBuf, ref: Option[String])(implicit mapping: mutable.Map[String, Any]): Unit = {
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

  /**
    * Except a given character.
    *
    * @param buf current bytebuf.
    * @param ch  excepted character.
    */
  private def expect(buf: ByteBuf, ch: Char): Unit = {
    if (buf.readByte != ch) {
      throw new StreamException(s"Expected $ch at index ${buf.readerIndex}", Some(JsString(buf.toString)))
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

  /**
    * Create a syslog input RCF5424 compilant.
    *
    * @param config input configuration.
    * @return syslog input RCF5424 compilant.
    */
  def createRFC5424(config: RFC5424Config): ((ByteString) => JsObject) = new SyslogRFC5424Input(config)

}
