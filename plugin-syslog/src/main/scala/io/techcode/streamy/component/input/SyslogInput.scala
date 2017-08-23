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
import io.techcode.streamy.component.Input
import io.techcode.streamy.component.input.SyslogInput.RFC5424Config
import io.techcode.streamy.stream.StreamException
import play.api.libs.json.{JsObject, JsString, Json}

import scala.collection.mutable


/**
  * Syslog RFC5424 input implementation.
  */
private[input] class SyslogRFC5424Input(config: RFC5424Config) extends Input[JsObject] {

  override def apply(pkt: ByteString): JsObject = {
    // Grab new buffer
    val buf: ByteBuf = new ByteBuf(pkt)

    // Populate
    val mapping: mutable.Map[String, String] = new mutable.LinkedHashMap[String, String]

    // Read PRIVAL
    expect(buf, '<')
    capture(buf, config.facility, FindCloseQuote, mapping)

    // Read version
    expect(buf, '1')
    expect(buf, ' ')

    // Read timestamp
    capture(buf, config.timestamp, FindSpace, mapping)

    // Read hostname
    capture(buf, config.hostname, FindSpace, mapping)

    // Read app name
    capture(buf, config.app, FindSpace, mapping)

    // Read proc id
    capture(buf, config.proc, FindSpace, mapping)

    // Read message id
    capture(buf, config.msgId, FindSpace, mapping)

    // Read structured data
    captureStructData(buf, config.structData, mapping)

    // Read message
    if (config.message.isDefined) {
      mapping.put(config.message.get, buf.slice().utf8String.trim)
    }

    // Split header and message
    JsObject(mapping.mapValues(JsString))
  }

  /**
    * Capture a syslog part.
    *
    * @param buf       current bytebuf
    * @param ref       reference part.
    * @param processor bytebuf processor used to delimite part.
    * @param mapping   mapping accumulator.
    */
  private def capture(buf: ByteBuf, ref: Option[String], processor: ByteBufProcessor, mapping: mutable.Map[String, String]): Unit = {
    if (ref.isDefined && buf.getByte != '-') {
      mapping.put(ref.get, buf.readString(processor))
    } else {
      buf.skipBytes(processor)
    }
  }

  /**
    * Capture a syslog part.
    *
    * @param buf     current bytebuf.
    * @param ref     reference part.
    * @param mapping mapping accumulator.
    */
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

  /**
    * Except a given character.
    *
    * @param buf current bytebuf.
    * @param ch  excepted character.
    */
  private def expect(buf: ByteBuf, ch: Char): Unit = {
    if (buf.readByte != ch) {
      throw new StreamException(s"Expected $ch at index ${buf.readerIndex}", Some(Json.obj("message" -> buf.toString)))
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
    val MsgId = "msgId"
    val StructData = "structDataId"
    val Message = "message"
  }

  // Component configuration
  case class RFC5424Config(
    facility: Option[String] = None,
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
  def createRFC5424(config: RFC5424Config): Input[JsObject] = new SyslogRFC5424Input(config)

}
