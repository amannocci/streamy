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
import io.techcode.streamy.component.output.SyslogOutput.{RFC3164Config, RFC5424Config}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.{JsObject, Json}

/**
  * Syslog output spec.
  */
class SyslogOutputSpec extends FlatSpec with Matchers {

  "Syslog RFC5424 output" must "format correctly simple syslog message" in {
    val input = SyslogOutput.rfc5424(SyslogOutputSpec.RFC5424FormatAll)
    val result = input.apply(SyslogOutputSpec.RFC5424Simple)
    result should equal(ByteString("<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 - 'su root' failed for lonvick on /dev/pts/8"))
  }

  it must "format correctly when only facility is set" in {
    val input = SyslogOutput.rfc5424(SyslogOutputSpec.RFC5424FormatFacility)
    val result = input.apply(SyslogOutputSpec.RFC5424Simple)
    result should equal(ByteString("<38>1 1970-01-01T00:00:00.000Z - - - - -"))
  }

  it must "format correctly when only severity is set" in {
    val input = SyslogOutput.rfc5424(SyslogOutputSpec.RFC5424FormatSeverity)
    val result = input.apply(SyslogOutputSpec.RFC5424Simple)
    result should equal(ByteString("<26>1 1970-01-01T00:00:00.000Z - - - - -"))
  }

  it must "format correctly when only timestamp is set" in {
    val input = SyslogOutput.rfc5424(SyslogOutputSpec.RFC5424FormatTimestamp)
    val result = input.apply(SyslogOutputSpec.RFC5424Simple)
    result should equal(ByteString("<30>1 2003-10-11T22:14:15.003Z - - - - -"))
  }

  it must "format correctly when only hostname is set" in {
    val input = SyslogOutput.rfc5424(SyslogOutputSpec.RFC5424FormatHostname)
    val result = input.apply(SyslogOutputSpec.RFC5424Simple)
    result should equal(ByteString("<30>1 1970-01-01T00:00:00.000Z mymachine.example.com - - - -"))
  }

  it must "format correctly when only app is set" in {
    val input = SyslogOutput.rfc5424(SyslogOutputSpec.RFC5424FormatApp)
    val result = input.apply(SyslogOutputSpec.RFC5424Simple)
    result should equal(ByteString("<30>1 1970-01-01T00:00:00.000Z - su - - -"))
  }

  it must "format correctly when only proc is set" in {
    val input = SyslogOutput.rfc5424(SyslogOutputSpec.RFC5424FormatProc)
    val result = input.apply(SyslogOutputSpec.RFC5424Simple)
    result should equal(ByteString("<30>1 1970-01-01T00:00:00.000Z - - 77042 - -"))
  }

  it must "format correctly when only msgId is set" in {
    val input = SyslogOutput.rfc5424(SyslogOutputSpec.RFC5424FormatMsgId)
    val result = input.apply(SyslogOutputSpec.RFC5424Simple)
    result should equal(ByteString("<30>1 1970-01-01T00:00:00.000Z - - - ID47 -"))
  }

  it must "format correctly when only message is set" in {
    val input = SyslogOutput.rfc5424(SyslogOutputSpec.RFC5424FormatMessage)
    val result = input.apply(SyslogOutputSpec.RFC5424Simple)
    result should equal(ByteString("<30>1 1970-01-01T00:00:00.000Z - - - - - 'su root' failed for lonvick on /dev/pts/8"))
  }

  "Syslog RFC3164 output" must "format correctly simple syslog message" in {
    val input = SyslogOutput.rfc3164(SyslogOutputSpec.RFC3164FormatAll)
    val result = input.apply(SyslogOutputSpec.RFC3164Simple)
    result should equal(ByteString("<34>Aug 24 05:34:00 mymachine.example.com su[77042]: 'su root' failed for lonvick on /dev/pts/8\n"))
  }

  it must "format correctly when only facility is set" in {
    val input = SyslogOutput.rfc3164(SyslogOutputSpec.RFC3164FormatFacility)
    val result = input.apply(SyslogOutputSpec.RFC3164Simple)
    result should equal(ByteString(s"<38>Jan 1 00:00:00.000 ${SyslogOutputSpec.Hostname} streamy[1]\n"))
  }

  it must "format correctly when only severity is set" in {
    val input = SyslogOutput.rfc3164(SyslogOutputSpec.RFC3164FormatSeverity)
    val result = input.apply(SyslogOutputSpec.RFC3164Simple)
    result should equal(ByteString(s"<26>Jan 1 00:00:00.000 ${SyslogOutputSpec.Hostname} streamy[1]\n"))
  }

  it must "format correctly when only timestamp is set" in {
    val input = SyslogOutput.rfc3164(SyslogOutputSpec.RFC3164FormatTimestamp)
    val result = input.apply(SyslogOutputSpec.RFC3164Simple)
    result should equal(ByteString(s"<30>Aug 24 05:34:00 ${SyslogOutputSpec.Hostname} streamy[1]\n"))
  }

  it must "format correctly when only hostname is set" in {
    val input = SyslogOutput.rfc3164(SyslogOutputSpec.RFC3164FormatHostname)
    val result = input.apply(SyslogOutputSpec.RFC3164Simple)
    result should equal(ByteString("<30>Jan 1 00:00:00.000 mymachine.example.com streamy[1]\n"))
  }

  it must "format correctly when only app is set" in {
    val input = SyslogOutput.rfc3164(SyslogOutputSpec.RFC3164FormatApp)
    val result = input.apply(SyslogOutputSpec.RFC3164Simple)
    result should equal(ByteString(s"<30>Jan 1 00:00:00.000 ${SyslogOutputSpec.Hostname} su[1]\n"))
  }

  it must "format correctly when only proc is set" in {
    val input = SyslogOutput.rfc3164(SyslogOutputSpec.RFC3164FormatProc)
    val result = input.apply(SyslogOutputSpec.RFC3164Simple)
    result should equal(ByteString(s"<30>Jan 1 00:00:00.000 ${SyslogOutputSpec.Hostname} streamy[77042]\n"))
  }

  it must "format correctly when only message is set" in {
    val input = SyslogOutput.rfc3164(SyslogOutputSpec.RFC3164FormatMessage)
    val result = input.apply(SyslogOutputSpec.RFC3164Simple)
    result should equal(ByteString(s"<30>Jan 1 00:00:00.000 ${SyslogOutputSpec.Hostname} streamy[1]: 'su root' failed for lonvick on /dev/pts/8\n"))
  }

}

object SyslogOutputSpec {
  val RFC5424FormatAll = RFC5424Config(
    facility = Some(SyslogOutput.Id.Facility),
    severity = Some(SyslogOutput.Id.Severity),
    timestamp = Some(SyslogOutput.Id.Timestamp),
    hostname = Some(SyslogOutput.Id.Hostname),
    app = Some(SyslogOutput.Id.App),
    proc = Some(SyslogOutput.Id.Proc),
    msgId = Some(SyslogOutput.Id.MsgId),
    message = Some(SyslogOutput.Id.Message)
  )
  val RFC5424FormatFacility = RFC5424Config(facility = Some(SyslogOutput.Id.Facility))
  val RFC5424FormatSeverity = RFC5424Config(severity = Some(SyslogOutput.Id.Severity))
  val RFC5424FormatTimestamp = RFC5424Config(timestamp = Some(SyslogOutput.Id.Timestamp))
  val RFC5424FormatHostname = RFC5424Config(hostname = Some(SyslogOutput.Id.Hostname))
  val RFC5424FormatApp = RFC5424Config(app = Some(SyslogOutput.Id.App))
  val RFC5424FormatProc = RFC5424Config(proc = Some(SyslogOutput.Id.Proc))
  val RFC5424FormatMsgId = RFC5424Config(msgId = Some(SyslogOutput.Id.MsgId))
  val RFC5424FormatMessage = RFC5424Config(message = Some(SyslogOutput.Id.Message))

  val RFC5424Simple: JsObject = Json.obj(
    SyslogOutput.Id.Facility -> 4,
    SyslogOutput.Id.Severity -> 2,
    SyslogOutput.Id.Timestamp -> "2003-10-11T22:14:15.003Z",
    SyslogOutput.Id.Hostname -> "mymachine.example.com",
    SyslogOutput.Id.App -> "su",
    SyslogOutput.Id.Proc -> "77042",
    SyslogOutput.Id.MsgId -> "ID47",
    SyslogOutput.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
  )

  val Hostname: String = InetAddress.getLocalHost.getHostName

  val RFC3164FormatAll = RFC3164Config(
    facility = Some(SyslogOutput.Id.Facility),
    severity = Some(SyslogOutput.Id.Severity),
    timestamp = Some(SyslogOutput.Id.Timestamp),
    hostname = Some(SyslogOutput.Id.Hostname),
    app = Some(SyslogOutput.Id.App),
    proc = Some(SyslogOutput.Id.Proc),
    message = Some(SyslogOutput.Id.Message)
  )
  val RFC3164FormatFacility = RFC3164Config(facility = Some(SyslogOutput.Id.Facility))
  val RFC3164FormatSeverity = RFC3164Config(severity = Some(SyslogOutput.Id.Severity))
  val RFC3164FormatTimestamp = RFC3164Config(timestamp = Some(SyslogOutput.Id.Timestamp))
  val RFC3164FormatHostname = RFC3164Config(hostname = Some(SyslogOutput.Id.Hostname))
  val RFC3164FormatApp = RFC3164Config(app = Some(SyslogOutput.Id.App))
  val RFC3164FormatProc = RFC3164Config(proc = Some(SyslogOutput.Id.Proc))
  val RFC3164FormatMessage = RFC3164Config(message = Some(SyslogOutput.Id.Message))

  val RFC3164Simple: JsObject = Json.obj(
    SyslogOutput.Id.Facility -> 4,
    SyslogOutput.Id.Severity -> 2,
    SyslogOutput.Id.Timestamp -> "Aug 24 05:34:00",
    SyslogOutput.Id.Hostname -> "mymachine.example.com",
    SyslogOutput.Id.App -> "su",
    SyslogOutput.Id.Proc -> "77042",
    SyslogOutput.Id.MsgId -> "ID47",
    SyslogOutput.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
  )
}
