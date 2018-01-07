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
package io.techcode.streamy.util.printer

import java.net.InetAddress

import akka.util.ByteString
import io.techcode.streamy.component.transformer.SyslogTransformer._
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.Binder
import org.apache.commons.lang3.StringUtils

/**
  * Syslog printer companion.
  */
object SyslogPrinter {

  val Nil: String = "-"
  val NilByte: Byte = '-'.toByte
  val Space: Byte = ' '
  val Date: String = "1970-01-01T00:00:00.000Z"
  val DateStamp: String = "Jan 1 00:00:00.000"
  val ProcId: String = "1"
  val AppName: String = "streamy"
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

  /**
    * Create a syslog printer that transform incoming [[Json]] to [[ByteString]].
    * This printer is Rfc5424 compliant.
    *
    * @param pkt     data to print.
    * @param binding binding printer configuration.
    * @return new syslog printer Rfc5424 compliant.
    */
  def rfc5424(pkt: Json, binding: Rfc5424.Binding): JsonPrinter = new Rfc5424Printer(pkt, binding)

  /**
    * Create a syslog printer that transform incoming [[Json]] to [[ByteString]].
    * This printer is Rfc3164 compliant.
    *
    * @param pkt     data to print.
    * @param binding binding printer configuration.
    * @return new syslog printer Rfc3164 compliant.
    */
  def rfc3164(pkt: Json, binding: Rfc3164.Binding): JsonPrinter = new Rfc3164Printer(pkt, binding)

}

/**
  * Printer helpers containing various shortcut for printing.
  */
private abstract class PrinterHelpers(pkt: Json) extends JsonPrinter(pkt) {

  /**
    * Print data part to format syslog message.
    *
    * @param conf         name of the field.
    * @param defaultValue default value.
    */
  def printWithDefault(conf: Option[Binder], defaultValue: String): Unit = {
    if (conf.isDefined) {
      val binder = conf.get
      builder.append(binder.bind(pkt.evaluate(Root / binder.key).getOrElse(defaultValue)))
    } else {
      builder.putBytes(defaultValue.getBytes)
    }
  }

  /**
    * Populate data part to format syslog message.
    *
    * @param facilityConf configuration for facility.
    * @param severityConf configuration for severity.
    */
  def printPrival(facilityConf: Option[Binder], severityConf: Option[Binder]): Unit = {
    var prival = 0
    if (severityConf.isDefined) {
      val binder = severityConf.get
      prival = pkt.evaluate(Root / binder.key).flatMap(_.asInt).getOrElse(SyslogPrinter.Facility)
    } else {
      prival = SyslogPrinter.Severity
    }
    if (facilityConf.isDefined) {
      val binder = facilityConf.get
      prival += pkt.evaluate(Root / binder.key).flatMap(_.asInt).getOrElse(SyslogPrinter.Facility) << 3
    } else {
      prival += SyslogPrinter.Facility << 3
    }
    builder.putBytes(prival.toString.getBytes)
  }

}

/**
  * Syslog printer that transform incoming [[Json]] to [[ByteString]].
  * This printer is Rfc3164 compliant.
  *
  * @param pkt     data to print.
  * @param binding binding printer configuration.
  */
private class Rfc3164Printer(pkt: Json, binding: Rfc3164.Binding) extends PrinterHelpers(pkt) {

  override def process(): Boolean = {
    // Add PRIVAL
    builder.putByte(SyslogPrinter.Inf)
    printPrival(binding.facility, binding.severity)
    builder.putByte(SyslogPrinter.Sup)

    // Add timestamp
    printWithDefault(binding.timestamp, SyslogPrinter.DateStamp)
    builder.putByte(SyslogPrinter.Space)

    // Add hostname
    printWithDefault(binding.hostname, SyslogPrinter.HostName)
    builder.putByte(SyslogPrinter.Space)

    // Add app name
    printWithDefault(binding.appName, SyslogPrinter.AppName)

    // Add proc id
    builder.putByte(SyslogPrinter.OpenBracket)
    printWithDefault(binding.procId, SyslogPrinter.ProcId)
    builder.putByte(SyslogPrinter.CloseBracket)

    // Add message
    if (binding.message.isDefined) {
      builder.putByte(SyslogPrinter.SemiColon)
      builder.putByte(SyslogPrinter.Space)
      val binder = binding.message.get
      builder.append(binder.bind(pkt.evaluate(Root / binder.key).getOrElse(JsString(StringUtils.EMPTY))))
    }
    builder.putByte(SyslogPrinter.NewLine)
    true
  }

}

/**
  * Syslog printer that transform incoming [[Json]] to [[ByteString]].
  * This printer is Rfc5424 compliant.
  *
  * @param pkt     data to print.
  * @param binding binding printer configuration.
  */
private class Rfc5424Printer(pkt: Json, binding: Rfc5424.Binding) extends PrinterHelpers(pkt) {

  override def process(): Boolean = {
    // Add PRIVAL
    builder.putByte(SyslogPrinter.Inf)
    printPrival(binding.facility, binding.severity)
    builder.putByte(SyslogPrinter.Sup)

    // Add version
    builder.putBytes(SyslogPrinter.Version.getBytes())

    // Add timestamp
    printWithDefault(binding.timestamp, SyslogPrinter.Date)
    builder.putByte(SyslogPrinter.Space)

    // Add hostname
    printWithDefault(binding.hostname, SyslogPrinter.Nil)
    builder.putByte(SyslogPrinter.Space)

    // Add app name
    printWithDefault(binding.appName, SyslogPrinter.Nil)
    builder.putByte(SyslogPrinter.Space)

    // Add proc id
    printWithDefault(binding.procId, SyslogPrinter.Nil)
    builder.putByte(SyslogPrinter.Space)

    // Add message id
    printWithDefault(binding.msgId, SyslogPrinter.Nil)
    builder.putByte(SyslogPrinter.Space)

    // Skip structured data
    builder.putByte(SyslogPrinter.NilByte)

    // Add message
    if (binding.message.isDefined) {
      builder.putByte(SyslogPrinter.Space)
      val binder = binding.message.get
      builder.append(binder.bind(pkt.evaluate(Root / binder.key).getOrElse(JsString(StringUtils.EMPTY))))
    }
    true
  }

}
