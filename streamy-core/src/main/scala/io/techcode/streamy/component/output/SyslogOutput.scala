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
package io.techcode.streamy.component.output

import java.net.InetAddress

import akka.util.ByteString
import play.api.libs.json.JsObject

/**
  * Syslog RFC3164 output implementation.
  */
private[output] class SyslogRFC3164Output(spec: Map[String, String]) extends Output[JsObject] {

  // Default hostname
  private val hostName = InetAddress.getLocalHost.getHostName

  // Set all pref
  val facility: Option[String] = spec.get(SyslogOutput.FacilityId).orElse(Option(SyslogOutput.FacilityId))
  val timestamp: Option[String] = spec.get(SyslogOutput.TimestampId).orElse(Option(SyslogOutput.TimestampId))
  val hostname: Option[String] = spec.get(SyslogOutput.HostnameId).orElse(Option(SyslogOutput.HostnameId))
  val app: Option[String] = spec.get(SyslogOutput.AppId).orElse(Option(SyslogOutput.AppId))
  val proc: Option[String] = spec.get(SyslogOutput.ProcId).orElse(Option(SyslogOutput.ProcId))
  val message: Option[String] = spec.get(SyslogOutput.MessageId).orElse(Option(SyslogOutput.MessageId))

  override def apply(pkt: JsObject): ByteString = {
    val buf = ByteString.createBuilder

    // Add PRIVAL
    buf.putByte(SyslogOutput.Inf)
    buf.putBytes((pkt \ facility.get).asOpt[String].getOrElse(SyslogOutput.Facility).getBytes)
    buf.putByte(SyslogOutput.Sup)

    // Add timestamp
    buf.putBytes((pkt \ timestamp.get).asOpt[String].getOrElse(SyslogOutput.DateStamp).getBytes)
    buf.putByte(SyslogOutput.Space)

    // Add hostname
    buf.putBytes((pkt \ hostname.get).asOpt[String].getOrElse(hostName).getBytes)
    buf.putByte(SyslogOutput.Space)

    // Add app name
    buf.putBytes((pkt \ app.get).asOpt[String].getOrElse(SyslogOutput.App).getBytes)

    // Add proc id
    buf.putByte(SyslogOutput.OpenBracket)
    buf.putBytes((pkt \ proc.get).asOpt[String].getOrElse(SyslogOutput.Proc).getBytes)
    buf.putByte(SyslogOutput.CloseBracket)
    buf.putByte(SyslogOutput.SemiColon)
    buf.putByte(SyslogOutput.Space)

    // Add message
    if (message.isDefined) {
      buf.putBytes((pkt \ message.get).as[String].getBytes)
    }
    buf.putByte(SyslogOutput.NewLine)

    // Build result
    buf.result()
  }

}

/**
  * Syslog RFC5424 output implementation.
  */
private[output] class SyslogRFC5424Output(spec: Map[String, String]) extends Output[JsObject] {

  // Set all pref
  val facility: Option[String] = spec.get(SyslogOutput.FacilityId).orElse(Option(SyslogOutput.FacilityId))
  val timestamp: Option[String] = spec.get(SyslogOutput.TimestampId).orElse(Option(SyslogOutput.TimestampId))
  val hostname: Option[String] = spec.get(SyslogOutput.HostnameId).orElse(Option(SyslogOutput.HostnameId))
  val app: Option[String] = spec.get(SyslogOutput.AppId).orElse(Option(SyslogOutput.AppId))
  val proc: Option[String] = spec.get(SyslogOutput.ProcId).orElse(Option(SyslogOutput.ProcId))
  val msgId: Option[String] = spec.get(SyslogOutput.MsgId).orElse(Option(SyslogOutput.MsgId))
  val message: Option[String] = spec.get(SyslogOutput.MessageId).orElse(Option(SyslogOutput.MessageId))

  override def apply(pkt: JsObject): ByteString = {
    val buf = ByteString.createBuilder

    // Add PRIVAL
    buf.putByte(SyslogOutput.Inf)
    buf.putBytes((pkt \ facility.get).asOpt[String].getOrElse(SyslogOutput.Facility).getBytes)
    buf.putByte(SyslogOutput.Sup)

    // Add version
    buf.putBytes(SyslogOutput.Version.getBytes())

    // Add timestamp
    buf.putBytes((pkt \ timestamp.get).asOpt[String].getOrElse(SyslogOutput.Date).getBytes)
    buf.putByte(SyslogOutput.Space)

    // Add hostname
    buf.putBytes((pkt \ hostname.get).asOpt[String].getOrElse(SyslogOutput.Nil).getBytes)
    buf.putByte(SyslogOutput.Space)

    // Add app name
    buf.putBytes((pkt \ app.get).asOpt[String].getOrElse(SyslogOutput.Nil).getBytes)
    buf.putByte(SyslogOutput.Space)

    // Add proc id
    buf.putBytes((pkt \ proc.get).asOpt[String].getOrElse(SyslogOutput.Nil).getBytes)
    buf.putByte(SyslogOutput.Space)

    // Add message id
    buf.putBytes((pkt \ msgId.get).asOpt[String].getOrElse(SyslogOutput.Nil).getBytes)
    buf.putByte(SyslogOutput.Space)

    // Skip structured data
    buf.putByte(SyslogOutput.NilByte)
    buf.putByte(SyslogOutput.Space)

    // Add message
    if (message.isDefined) {
      buf.putBytes((pkt \ message.get).as[String].getBytes)
    }

    // Build result
    buf.result()
  }

}

/**
  * Some constants
  */
object SyslogOutput {
  val Nil: String = "-"
  val NilByte: Byte = '-'.toByte
  val Space: Byte = ' '
  val Date: String = "1970-01-01T00:00:00.000Z"
  val DateStamp: String = "Jan 1 00:00:00.000"
  val Proc: String = "1"
  val App: String = "streamy"
  val Facility: String = "30"
  val SemiColon: Byte = ':'
  val NewLine: Byte = '\n'
  val Inf: Byte = '<'
  val Sup: Byte = '>'
  val OpenBracket: Byte = '['
  val CloseBracket: Byte = ']'
  val Version: String = "1 "

  val FacilityId = "facility"
  val TimestampId = "timestamp"
  val HostnameId = "hostname"
  val AppId = "app"
  val ProcId = "proc"
  val MsgId = "msgId"
  val MessageId = "message"

  /**
    * Create a syslog output RCF5424 compilant.
    *
    * @param spec enable features.
    * @return syslog output RCF5424 compilant.
    */
  def createRFC5424(spec: Map[String, String]): Output[JsObject] = new SyslogRFC5424Output(spec)

  /**
    * Create a syslog output RCF3126 compilant.
    *
    * @param spec enable features.
    * @return syslog output RCF3126 compilant.
    */
  def createRFC3164(spec: Map[String, String]): Output[JsObject] = new SyslogRFC3164Output(spec)

}
