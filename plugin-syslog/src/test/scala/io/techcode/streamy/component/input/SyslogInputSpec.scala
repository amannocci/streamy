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
import gnieh.diffson.Pointer._
import gnieh.diffson.circe._
import io.circe._
import io.techcode.streamy.component.input.SyslogInput.RFC5424Config
import io.techcode.streamy.stream.StreamException
import org.scalatest.{FlatSpec, Matchers}

/**
  * Syslog input spec.
  */
class SyslogInputSpec extends FlatSpec with Matchers {

  "Syslog RFC5424 input" must "handle correctly simple syslog message" in {
    val input = SyslogInput.rfc5424(SyslogInputSpec.CaptureAll)
    val result = input.apply(SyslogInputSpec.Simple)
    pointer.evaluate(result, root / SyslogInput.Id.Facility) should equal(Json.fromInt(4))
    pointer.evaluate(result, root / SyslogInput.Id.Severity) should equal(Json.fromInt(2))
    pointer.evaluate(result, root / SyslogInput.Id.Timestamp) should equal(Json.fromString("2003-10-11T22:14:15.003Z"))
    pointer.evaluate(result, root / SyslogInput.Id.Hostname) should equal(Json.fromString("mymachine.example.com"))
    pointer.evaluate(result, root / SyslogInput.Id.App) should equal(Json.fromString("su"))
    pointer.evaluate(result, root / SyslogInput.Id.Proc) should equal(Json.fromString("77042"))
    pointer.evaluate(result, root / SyslogInput.Id.MsgId) should equal(Json.fromString("ID47"))
    pointer.evaluate(result, root / SyslogInput.Id.StructData) should equal(Json.fromString("""sigSig ver="1""""))
    pointer.evaluate(result, root / SyslogInput.Id.Message) should equal(Json.fromString("'su root' failed for lonvick on /dev/pts/8"))
  }

  it must "capture only facility when set" in {
    val input = SyslogInput.rfc5424(SyslogInputSpec.CaptureFacility)
    val result = input.apply(SyslogInputSpec.Simple)
    result.asObject.get.fields.size should equal(1)
    pointer.evaluate(result, root / SyslogInput.Id.Facility) should equal(Json.fromInt(4))
  }

  it must "capture only severity when set" in {
    val input = SyslogInput.rfc5424(SyslogInputSpec.CaptureSeverity)
    val result = input.apply(SyslogInputSpec.Simple)
    result.asObject.get.fields.size should equal(1)
    pointer.evaluate(result, root / SyslogInput.Id.Severity) should equal(Json.fromInt(2))
  }

  it must "capture only timestamp when set" in {
    val input = SyslogInput.rfc5424(SyslogInputSpec.CaptureTimestamp)
    val result = input.apply(SyslogInputSpec.Simple)
    result.asObject.get.fields.size should equal(1)
    pointer.evaluate(result, root / SyslogInput.Id.Timestamp) should equal(Json.fromString("2003-10-11T22:14:15.003Z"))
  }

  it must "capture only hostname when set" in {
    val input = SyslogInput.rfc5424(SyslogInputSpec.CaptureHostname)
    val result = input.apply(SyslogInputSpec.Simple)
    result.asObject.get.fields.size should equal(1)
    pointer.evaluate(result, root / SyslogInput.Id.Hostname) should equal(Json.fromString("mymachine.example.com"))
  }

  it must "capture only app when set" in {
    val input = SyslogInput.rfc5424(SyslogInputSpec.CaptureApp)
    val result = input.apply(SyslogInputSpec.Simple)
    result.asObject.get.fields.size should equal(1)
    pointer.evaluate(result, root / SyslogInput.Id.App) should equal(Json.fromString("su"))
  }

  it must "capture only proc when set" in {
    val input = SyslogInput.rfc5424(SyslogInputSpec.CaptureProc)
    val result = input.apply(SyslogInputSpec.Simple)
    result.asObject.get.fields.size should equal(1)
    pointer.evaluate(result, root / SyslogInput.Id.Proc) should equal(Json.fromString("77042"))
  }

  it must "capture only msg when set" in {
    val input = SyslogInput.rfc5424(SyslogInputSpec.CaptureMsg)
    val result = input.apply(SyslogInputSpec.Simple)
    result.asObject.get.fields.size should equal(1)
    pointer.evaluate(result, root / SyslogInput.Id.MsgId) should equal(Json.fromString("ID47"))
  }

  it must "capture only struct data when set" in {
    val input = SyslogInput.rfc5424(SyslogInputSpec.CaptureStructData)
    val result = input.apply(SyslogInputSpec.Simple)
    result.asObject.get.fields.size should equal(1)
    pointer.evaluate(result, root / SyslogInput.Id.StructData) should equal(Json.fromString("""sigSig ver="1""""))
  }

  it must "capture only message when set" in {
    val input = SyslogInput.rfc5424(SyslogInputSpec.CaptureMessage)
    val result = input.apply(SyslogInputSpec.Simple)
    result.asObject.get.fields.size should equal(1)
    pointer.evaluate(result, root / SyslogInput.Id.Message) should equal(Json.fromString("'su root' failed for lonvick on /dev/pts/8"))
  }

  it must "throw an error when syslog message is malformed" in {
    val input = SyslogInput.rfc5424(SyslogInputSpec.CaptureAll)
    assertThrows[StreamException] {
      input.apply(SyslogInputSpec.Malformed)
    }
  }

}

object SyslogInputSpec {
  val CaptureAll = RFC5424Config(
    facility = Some(SyslogInput.Id.Facility),
    severity = Some(SyslogInput.Id.Severity),
    timestamp = Some(SyslogInput.Id.Timestamp),
    hostname = Some(SyslogInput.Id.Hostname),
    app = Some(SyslogInput.Id.App),
    proc = Some(SyslogInput.Id.Proc),
    msgId = Some(SyslogInput.Id.MsgId),
    structData = Some(SyslogInput.Id.StructData),
    message = Some(SyslogInput.Id.Message)
  )
  val CaptureFacility = RFC5424Config(facility = Some(SyslogInput.Id.Facility))
  val CaptureSeverity = RFC5424Config(severity = Some(SyslogInput.Id.Severity))
  val CaptureTimestamp = RFC5424Config(timestamp = Some(SyslogInput.Id.Timestamp))
  val CaptureHostname = RFC5424Config(hostname = Some(SyslogInput.Id.Hostname))
  val CaptureApp = RFC5424Config(app = Some(SyslogInput.Id.App))
  val CaptureProc = RFC5424Config(proc = Some(SyslogInput.Id.Proc))
  val CaptureMsg = RFC5424Config(msgId = Some(SyslogInput.Id.MsgId))
  val CaptureStructData = RFC5424Config(structData = Some(SyslogInput.Id.StructData))
  val CaptureMessage = RFC5424Config(message = Some(SyslogInput.Id.Message))

  val Simple = ByteString("""<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
  val Malformed = ByteString("""<34> 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
}