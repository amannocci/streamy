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
import io.techcode.streamy.component.transformer.SyslogInput.RFC5424Config
import io.techcode.streamy.stream.StreamException
import io.techcode.streamy.util.json._
import org.scalatest.{Matchers, WordSpecLike}

/**
  * Syslog input spec.
  */
class SyslogInputSpec extends WordSpecLike with Matchers {

  "Syslog RFC5424 input" should {
    "handle correctly simple syslog message" in {
      val input = new SyslogRFC5424Input(SyslogInputSpec.CaptureAll)
      val result = input.apply(SyslogInputSpec.Simple)
      result.evaluate(Root / SyslogInput.Id.Facility) should equal(Some(intToJson(4)))
      result.evaluate(Root / SyslogInput.Id.Severity) should equal(Some(intToJson(2)))
      result.evaluate(Root / SyslogInput.Id.Timestamp) should equal(Some(stringToJson("2003-10-11T22:14:15.003Z")))
      result.evaluate(Root / SyslogInput.Id.Hostname) should equal(Some(stringToJson("mymachine.example.com")))
      result.evaluate(Root / SyslogInput.Id.App) should equal(Some(stringToJson("su")))
      result.evaluate(Root / SyslogInput.Id.Proc) should equal(Some(stringToJson("77042")))
      result.evaluate(Root / SyslogInput.Id.MsgId) should equal(Some(stringToJson("ID47")))
      result.evaluate(Root / SyslogInput.Id.StructData) should equal(Some(stringToJson("""sigSig ver="1"""")))
      result.evaluate(Root / SyslogInput.Id.Message) should equal(Some(stringToJson("'su root' failed for lonvick on /dev/pts/8")))
    }

    "capture only facility when set" in {
      val input = new SyslogRFC5424Input(SyslogInputSpec.CaptureFacility)
      val result = input.apply(SyslogInputSpec.Simple)
      result.asObject.get.fields.size should equal(1)
      result.evaluate(Root / SyslogInput.Id.Facility) should equal(Some(intToJson(4)))
    }

    "capture only severity when set" in {
      val input = new SyslogRFC5424Input(SyslogInputSpec.CaptureSeverity)
      val result = input.apply(SyslogInputSpec.Simple)
      result.asObject.get.fields.size should equal(1)
      result.evaluate(Root / SyslogInput.Id.Severity) should equal(Some(intToJson(2)))
    }

    "capture only timestamp when set" in {
      val input = new SyslogRFC5424Input(SyslogInputSpec.CaptureTimestamp)
      val result = input.apply(SyslogInputSpec.Simple)
      result.asObject.get.fields.size should equal(1)
      result.evaluate(Root / SyslogInput.Id.Timestamp) should equal(Some(stringToJson("2003-10-11T22:14:15.003Z")))
    }

    "capture only hostname when set" in {
      val input = new SyslogRFC5424Input(SyslogInputSpec.CaptureHostname)
      val result = input.apply(SyslogInputSpec.Simple)
      result.asObject.get.fields.size should equal(1)
      result.evaluate(Root / SyslogInput.Id.Hostname) should equal(Some(stringToJson("mymachine.example.com")))
    }

    "capture only app when set" in {
      val input = new SyslogRFC5424Input(SyslogInputSpec.CaptureApp)
      val result = input.apply(SyslogInputSpec.Simple)
      result.asObject.get.fields.size should equal(1)
      result.evaluate(Root / SyslogInput.Id.App) should equal(Some(stringToJson("su")))
    }

    "capture only proc when set" in {
      val input = new SyslogRFC5424Input(SyslogInputSpec.CaptureProc)
      val result = input.apply(SyslogInputSpec.Simple)
      result.asObject.get.fields.size should equal(1)
      result.evaluate(Root / SyslogInput.Id.Proc) should equal(Some(stringToJson("77042")))
    }

    "capture only msg when set" in {
      val input = new SyslogRFC5424Input(SyslogInputSpec.CaptureMsg)
      val result = input.apply(SyslogInputSpec.Simple)
      result.asObject.get.fields.size should equal(1)
      result.evaluate(Root / SyslogInput.Id.MsgId) should equal(Some(stringToJson("ID47")))
    }

    "capture only struct data when set" in {
      val input = new SyslogRFC5424Input(SyslogInputSpec.CaptureStructData)
      val result = input.apply(SyslogInputSpec.Simple)
      result.asObject.get.fields.size should equal(1)
      result.evaluate(Root / SyslogInput.Id.StructData) should equal(Some(stringToJson("""sigSig ver="1"""")))
    }

    "capture only message when set" in {
      val input = new SyslogRFC5424Input(SyslogInputSpec.CaptureMessage)
      val result = input.apply(SyslogInputSpec.Simple)
      result.asObject.get.fields.size should equal(1)
      result.evaluate(Root / SyslogInput.Id.Message) should equal(Some(stringToJson("'su root' failed for lonvick on /dev/pts/8")))
    }

    "throw an error when syslog message is malformed" in {
      val input = new SyslogRFC5424Input(SyslogInputSpec.CaptureAll)
      assertThrows[StreamException] {
        input.apply(SyslogInputSpec.Malformed)
      }
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