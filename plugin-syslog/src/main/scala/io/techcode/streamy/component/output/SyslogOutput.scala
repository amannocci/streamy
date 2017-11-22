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

import akka.util.{ByteString, ByteStringBuilder}
import io.techcode.streamy.component.output.SyslogOutput.{RFC3164Config, RFC5424Config}
import io.techcode.streamy.util.json._
import org.apache.commons.lang3.StringUtils

/**
  * Syslog RFC3164 output implementation.
  */
private[output] class SyslogRFC3164Output(config: RFC3164Config) extends (Json => ByteString) {

  override def apply(p: Json): ByteString = {
    implicit val pkt: Json = p
    implicit val buf: ByteStringBuilder = ByteString.newBuilder

    // Add PRIVAL
    buf.putByte(SyslogOutput.Inf)
    SyslogOutput.populatePrival(config.facility, config.severity)
    buf.putByte(SyslogOutput.Sup)

    // Add timestamp
    SyslogOutput.populate(config.timestamp, SyslogOutput.DateStamp)
    buf.putByte(SyslogOutput.Space)

    // Add hostname
    SyslogOutput.populate(config.hostname, SyslogOutput.HostName)
    buf.putByte(SyslogOutput.Space)

    // Add app name
    SyslogOutput.populate(config.app, SyslogOutput.App)

    // Add proc id
    buf.putByte(SyslogOutput.OpenBracket)
    SyslogOutput.populate(config.proc, SyslogOutput.Proc)
    buf.putByte(SyslogOutput.CloseBracket)

    // Add message
    if (config.message.isDefined) {
      buf.putByte(SyslogOutput.SemiColon)
      buf.putByte(SyslogOutput.Space)
      buf.putBytes(pkt.evaluate(Root / config.message.get).flatMap(_.asString).getOrElse(StringUtils.EMPTY).getBytes)
    }
    buf.putByte(SyslogOutput.NewLine)

    // Build result
    buf.result()
  }

}

/**
  * Syslog RFC5424 output implementation.
  */
private[output] class SyslogRFC5424Output(config: RFC5424Config) extends (Json => ByteString) {

  override def apply(p: Json): ByteString = {
    implicit val pkt: Json = p
    implicit val buf: ByteStringBuilder = ByteString.newBuilder

    // Add PRIVAL
    buf.putByte(SyslogOutput.Inf)
    SyslogOutput.populatePrival(config.facility, config.severity)
    buf.putByte(SyslogOutput.Sup)

    // Add version
    buf.putBytes(SyslogOutput.Version.getBytes())

    // Add timestamp
    SyslogOutput.populate(config.timestamp, SyslogOutput.Date)
    buf.putByte(SyslogOutput.Space)

    // Add hostname
    SyslogOutput.populate(config.hostname, SyslogOutput.Nil)
    buf.putByte(SyslogOutput.Space)

    // Add app name
    SyslogOutput.populate(config.app, SyslogOutput.Nil)
    buf.putByte(SyslogOutput.Space)

    // Add proc id
    SyslogOutput.populate(config.proc, SyslogOutput.Nil)
    buf.putByte(SyslogOutput.Space)

    // Add message id
    SyslogOutput.populate(config.msgId, SyslogOutput.Nil)
    buf.putByte(SyslogOutput.Space)

    // Skip structured data
    buf.putByte(SyslogOutput.NilByte)

    // Add message
    if (config.message.isDefined) {
      buf.putByte(SyslogOutput.Space)
      buf.putBytes(pkt.evaluate(Root / config.message.get).flatMap(_.asString).getOrElse(StringUtils.EMPTY).getBytes)
    }

    // Build result
    buf.result()
  }

}

/**
  * Some constants
  */
object SyslogOutput {

  object Id {
    val Facility = "facility"
    val Severity = "severity"
    val Timestamp = "timestamp"
    val Hostname = "hostname"
    val App = "app"
    val Proc = "proc"
    val MsgId = "msgId"
    val Message = "message"
  }

  val Nil: String = "-"
  val NilByte: Byte = '-'.toByte
  val Space: Byte = ' '
  val Date: String = "1970-01-01T00:00:00.000Z"
  val DateStamp: String = "Jan 1 00:00:00.000"
  val Proc: String = "1"
  val App: String = "streamy"
  val Facility: Int = 3
  val Severity: Int = 6
  val SemiColon: Byte = ':'
  val NewLine: Byte = '\n'
  val Inf: Byte = '<'
  val Sup: Byte = '>'
  val OpenBracket: Byte = '['
  val CloseBracket: Byte = ']'
  val Version: String = "1 "

  // Default hostname
  val HostName: String = InetAddress.getLocalHost.getHostName

  // Component configuration
  case class RFC5424Config(
    facility: Option[String] = None,
    severity: Option[String] = None,
    timestamp: Option[String] = None,
    hostname: Option[String] = None,
    app: Option[String] = None,
    proc: Option[String] = None,
    msgId: Option[String] = None,
    message: Option[String] = None
  )

  case class RFC3164Config(
    facility: Option[String] = None,
    severity: Option[String] = None,
    timestamp: Option[String] = None,
    hostname: Option[String] = None,
    app: Option[String] = None,
    proc: Option[String] = None,
    message: Option[String] = None
  )

  /**
    * Populate data part to format syslog message.
    *
    * @param conf         name of the field.
    * @param defaultValue default value.
    * @param pkt          packet involved.
    * @param buf          bytestring builder involved.
    */
  private[output] def populate(conf: Option[String], defaultValue: String)(implicit pkt: Json, buf: ByteStringBuilder): Unit = {
    conf match {
      case Some(key) => buf.putBytes(pkt.evaluate(Root / key).get.asString.getOrElse(defaultValue).getBytes())
      case None => buf.putBytes(defaultValue.getBytes())
    }
  }

  /**
    * Populate data part to format syslog message.
    *
    * @param facilityConf configuration for facility.
    * @param severityConf configuration for severity.
    * @param pkt          packet involved.
    * @param buf          bytestring builder involved.
    */
  private[output] def populatePrival(facilityConf: Option[String], severityConf: Option[String])(implicit pkt: Json, buf: ByteStringBuilder): Unit = {
    var prival = 0
    severityConf match {
      case Some(severityKey) => prival = pkt.evaluate(Root / severityKey).flatMap(_.asInt).getOrElse(SyslogOutput.Facility)
      case None => prival = SyslogOutput.Severity
    }
    facilityConf match {
      case Some(facilityKey) => prival += pkt.evaluate(Root / facilityKey).flatMap(_.asInt).getOrElse(SyslogOutput.Facility) << 3
      case None => prival += SyslogOutput.Facility << 3
    }
    buf.putBytes(prival.toString.getBytes)
  }

  /**
    * Create a syslog output RCF5424 compilant.
    *
    * @param config output configuration.
    * @return syslog output RCF5424 compilant.
    */
  def rfc5424(config: RFC5424Config): Json => ByteString = new SyslogRFC5424Output(config)

  /**
    * Create a syslog output RCF3126 compilant.
    *
    * @param config output configuration.
    * @return syslog output RCF3126 compilant.
    */
  def rfc3164(config: RFC3164Config): Json => ByteString = new SyslogRFC3164Output(config)

}
