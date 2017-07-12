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
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

/**
  * Syslog input spec.
  */
class SyslogInputSpec extends FlatSpec with Matchers {

  "Syslog RFC5424 input" must "handle correctly simple syslog message" in {
    val input = SyslogInput.createRFC5424(SyslogInputSpec.CaptureAll)
    val result = input.apply(SyslogInputSpec.Simple)
    (result \ SyslogInput.Id.Facility).get  should equal (Json.toJson("34"))
    (result \ SyslogInput.Id.Timestamp).get  should equal (Json.toJson("2003-10-11T22:14:15.003Z"))
    (result \ SyslogInput.Id.Hostname).get  should equal (Json.toJson("mymachine.example.com"))
    (result \ SyslogInput.Id.App).get  should equal (Json.toJson("su"))
    (result \ SyslogInput.Id.Proc).get  should equal (Json.toJson("77042"))
    (result \ SyslogInput.Id.Msg).get  should equal (Json.toJson("ID47"))
    (result \ SyslogInput.Id.StructData).get  should equal (Json.toJson("""sigSig ver="1""""))
    (result \ SyslogInput.Id.Message).get  should equal (Json.toJson("'su root' failed for lonvick on /dev/pts/8"))
  }

  it must "capture only facility when set" in {
    val input = SyslogInput.createRFC5424(SyslogInputSpec.CaptureFacility)
    val result = input.apply(SyslogInputSpec.Simple)
    result.fields.size should equal (1)
    (result \ SyslogInput.Id.Facility).get  should equal (Json.toJson("34"))
  }

  it must "capture only timestamp when set" in {
    val input = SyslogInput.createRFC5424(SyslogInputSpec.CaptureTimestamp)
    val result = input.apply(SyslogInputSpec.Simple)
    result.fields.size should equal (1)
    (result \ SyslogInput.Id.Timestamp).get  should equal (Json.toJson("2003-10-11T22:14:15.003Z"))
  }

  it must "capture only hostname when set" in {
    val input = SyslogInput.createRFC5424(SyslogInputSpec.CaptureHostname)
    val result = input.apply(SyslogInputSpec.Simple)
    result.fields.size should equal (1)
    (result \ SyslogInput.Id.Hostname).get  should equal (Json.toJson("mymachine.example.com"))
  }

  it must "capture only app when set" in {
    val input = SyslogInput.createRFC5424(SyslogInputSpec.CaptureApp)
    val result = input.apply(SyslogInputSpec.Simple)
    result.fields.size should equal (1)
    (result \ SyslogInput.Id.App).get  should equal (Json.toJson("su"))
  }

  it must "capture only proc when set" in {
    val input = SyslogInput.createRFC5424(SyslogInputSpec.CaptureProc)
    val result = input.apply(SyslogInputSpec.Simple)
    result.fields.size should equal (1)
    (result \ SyslogInput.Id.Proc).get  should equal (Json.toJson("77042"))
  }

  it must "capture only msg when set" in {
    val input = SyslogInput.createRFC5424(SyslogInputSpec.CaptureMsg)
    val result = input.apply(SyslogInputSpec.Simple)
    result.fields.size should equal (1)
    (result \ SyslogInput.Id.Msg).get  should equal (Json.toJson("ID47"))
  }

  it must "capture only struct data when set" in {
    val input = SyslogInput.createRFC5424(SyslogInputSpec.CaptureStructData)
    val result = input.apply(SyslogInputSpec.Simple)
    result.fields.size should equal (1)
    (result \ SyslogInput.Id.StructData).get  should equal (Json.toJson("""sigSig ver="1""""))
  }

  it must "capture only message when set" in {
    val input = SyslogInput.createRFC5424(SyslogInputSpec.CaptureMessage)
    val result = input.apply(SyslogInputSpec.Simple)
    result.fields.size should equal (1)
    (result \ SyslogInput.Id.Message).get  should equal (Json.toJson("'su root' failed for lonvick on /dev/pts/8"))
  }

}

object SyslogInputSpec {
  val CaptureAll = Map(
    SyslogInput.Id.Facility -> SyslogInput.Id.Facility,
    SyslogInput.Id.Timestamp -> SyslogInput.Id.Timestamp,
    SyslogInput.Id.Hostname -> SyslogInput.Id.Hostname,
    SyslogInput.Id.App -> SyslogInput.Id.App,
    SyslogInput.Id.Proc -> SyslogInput.Id.Proc,
    SyslogInput.Id.Msg -> SyslogInput.Id.Msg,
    SyslogInput.Id.StructData -> SyslogInput.Id.StructData,
    SyslogInput.Id.Message -> SyslogInput.Id.Message
  )
  val CaptureFacility = Map(SyslogInput.Id.Facility -> SyslogInput.Id.Facility)
  val CaptureTimestamp = Map(SyslogInput.Id.Timestamp -> SyslogInput.Id.Timestamp)
  val CaptureHostname = Map(SyslogInput.Id.Hostname -> SyslogInput.Id.Hostname)
  val CaptureApp = Map(SyslogInput.Id.App -> SyslogInput.Id.App)
  val CaptureProc = Map(SyslogInput.Id.Proc -> SyslogInput.Id.Proc)
  val CaptureMsg = Map(SyslogInput.Id.Msg -> SyslogInput.Id.Msg)
  val CaptureStructData = Map(SyslogInput.Id.StructData -> SyslogInput.Id.StructData)
  val CaptureMessage = Map(SyslogInput.Id.Message -> SyslogInput.Id.Message)

  val Simple = ByteString("""<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
}