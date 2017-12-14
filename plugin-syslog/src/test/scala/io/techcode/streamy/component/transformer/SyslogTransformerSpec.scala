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

import java.net.InetAddress

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
import io.techcode.streamy.component.transformer.SyslogTransformer.Rfc5424.Binding
import io.techcode.streamy.util.json._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Syslog transformer spec.
  */
class SyslogTransformerSpec extends TestKit(ActorSystem("SyslogTransformerSpec"))
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Syslog transformer" should {
    "in" should {
      "Rfc5424" should {
        "handle correctly simple syslog message" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.InputSimple)
            .via(SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureAll))
            .runWith(TestSink.probe[Json])
            .requestNext()

          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.Facility) should equal(Some(intToJson(4)))
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.Severity) should equal(Some(intToJson(2)))
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.Timestamp) should equal(Some(stringToJson("2003-10-11T22:14:15.003Z")))
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.Hostname) should equal(Some(stringToJson("mymachine.example.com")))
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.AppName) should equal(Some(stringToJson("su")))
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.ProcId) should equal(Some(stringToJson("77042")))
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.MsgId) should equal(Some(stringToJson("ID47")))
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.StructData) should equal(Some(stringToJson("""[sigSig ver="1"]""")))
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.Message) should equal(Some(stringToJson("'su root' failed for lonvick on /dev/pts/8")))
        }

        "capture only facility when set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.InputSimple)
            .via(SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureFacility))
            .runWith(TestSink.probe[Json])
            .requestNext()

          result.asObject.get.fields.size should equal(1)
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.Facility) should equal(Some(intToJson(4)))
        }

        "capture only severity when set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.InputSimple)
            .via(SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureSeverity))
            .runWith(TestSink.probe[Json])
            .requestNext()

          result.asObject.get.fields.size should equal(1)
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.Severity) should equal(Some(intToJson(2)))
        }

        "capture only timestamp when set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.InputSimple)
            .via(SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureTimestamp))
            .runWith(TestSink.probe[Json])
            .requestNext()

          result.asObject.get.fields.size should equal(1)
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.Timestamp) should equal(Some(stringToJson("2003-10-11T22:14:15.003Z")))
        }

        "capture only hostname when set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.InputSimple)
            .via(SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureHostname))
            .runWith(TestSink.probe[Json])
            .requestNext()

          result.asObject.get.fields.size should equal(1)
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.Hostname) should equal(Some(stringToJson("mymachine.example.com")))
        }

        "capture only app when set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.InputSimple)
            .via(SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureApp))
            .runWith(TestSink.probe[Json])
            .requestNext()

          result.asObject.get.fields.size should equal(1)
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.AppName) should equal(Some(stringToJson("su")))
        }

        "capture only proc when set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.InputSimple)
            .via(SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureProc))
            .runWith(TestSink.probe[Json])
            .requestNext()

          result.asObject.get.fields.size should equal(1)
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.ProcId) should equal(Some(stringToJson("77042")))
        }

        "capture only msg when set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.InputSimple)
            .via(SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureMsg))
            .runWith(TestSink.probe[Json])
            .requestNext()

          result.asObject.get.fields.size should equal(1)
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.MsgId) should equal(Some(stringToJson("ID47")))
        }

        "capture only struct data when set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.InputSimple)
            .via(SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureStructData))
            .runWith(TestSink.probe[Json])
            .requestNext()

          result.asObject.get.fields.size should equal(1)
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.StructData) should equal(Some(stringToJson("""[sigSig ver="1"]""")))
        }

        "capture only message when set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.InputSimple)
            .via(SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureMessage))
            .runWith(TestSink.probe[Json])
            .requestNext()

          result.asObject.get.fields.size should equal(1)
          result.evaluate(Root / SyslogTransformer.Rfc5424.Id.Message) should equal(Some(stringToJson("'su root' failed for lonvick on /dev/pts/8")))
        }

        "throw an error when syslog message is malformed" in {
          Source.single(SyslogTransformerSpec.Rfc5424.InputMalformed)
            .via(SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureAll))
            .runWith(TestSink.probe[Json])
            .request(1)
            .expectError()
        }
      }
    }

    "out" should {
      "Rfc3164" should {
        "format correctly simple syslog message" in {
          val result = Source.single(SyslogTransformerSpec.Rfc3164.OutputSimple)
            .via(SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatAll))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString("<34>Aug 24 05:34:00 mymachine.example.com su[77042]: 'su root' failed for lonvick on /dev/pts/8\n"))
        }

        "format correctly when only facility is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc3164.OutputSimple)
            .via(SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatFacility))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString(s"<38>Jan 1 00:00:00.000 ${SyslogTransformerSpec.Hostname} streamy[1]\n"))
        }

        "format correctly when only severity is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc3164.OutputSimple)
            .via(SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatSeverity))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString(s"<26>Jan 1 00:00:00.000 ${SyslogTransformerSpec.Hostname} streamy[1]\n"))
        }

        "format correctly when only timestamp is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc3164.OutputSimple)
            .via(SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatTimestamp))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString(s"<30>Aug 24 05:34:00 ${SyslogTransformerSpec.Hostname} streamy[1]\n"))
        }

        "format correctly when only hostname is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc3164.OutputSimple)
            .via(SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatHostname))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString("<30>Jan 1 00:00:00.000 mymachine.example.com streamy[1]\n"))
        }

        "format correctly when only app is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc3164.OutputSimple)
            .via(SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatApp))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString(s"<30>Jan 1 00:00:00.000 ${SyslogTransformerSpec.Hostname} su[1]\n"))
        }

        "format correctly when only proc is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc3164.OutputSimple)
            .via(SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatProc))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString(s"<30>Jan 1 00:00:00.000 ${SyslogTransformerSpec.Hostname} streamy[77042]\n"))
        }

        "format correctly when only message is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc3164.OutputSimple)
            .via(SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatMessage))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString(s"<30>Jan 1 00:00:00.000 ${SyslogTransformerSpec.Hostname} streamy[1]: 'su root' failed for lonvick on /dev/pts/8\n"))
        }
      }

      "Rfc5424" should {
        "format correctly simple syslog message" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.OutputSimple)
            .via(SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatAll))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString("<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 - 'su root' failed for lonvick on /dev/pts/8"))
        }

        "format correctly when only facility is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.OutputSimple)
            .via(SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatFacility))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString("<38>1 1970-01-01T00:00:00.000Z - - - - -"))
        }

        "format correctly when only severity is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.OutputSimple)
            .via(SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatSeverity))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString("<26>1 1970-01-01T00:00:00.000Z - - - - -"))
        }

        "format correctly when only timestamp is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.OutputSimple)
            .via(SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatTimestamp))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString("<30>1 2003-10-11T22:14:15.003Z - - - - -"))
        }

        "format correctly when only hostname is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.OutputSimple)
            .via(SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatHostname))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString("<30>1 1970-01-01T00:00:00.000Z mymachine.example.com - - - -"))
        }

        "format correctly when only app is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.OutputSimple)
            .via(SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatApp))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString("<30>1 1970-01-01T00:00:00.000Z - su - - -"))
        }

        "format correctly when only proc is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.OutputSimple)
            .via(SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatProc))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString("<30>1 1970-01-01T00:00:00.000Z - - 77042 - -"))
        }

        "format correctly when only msgId is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.OutputSimple)
            .via(SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatMsgId))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString("<30>1 1970-01-01T00:00:00.000Z - - - ID47 -"))
        }

        "format correctly when only message is set" in {
          val result = Source.single(SyslogTransformerSpec.Rfc5424.OutputSimple)
            .via(SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatMessage))
            .runWith(TestSink.probe[ByteString])
            .requestNext()

          result should equal(ByteString("<30>1 1970-01-01T00:00:00.000Z - - - - - 'su root' failed for lonvick on /dev/pts/8"))
        }
      }
    }
  }

}

object SyslogTransformerSpec {

  val Hostname: String = InetAddress.getLocalHost.getHostName

  object Rfc3164 {

    val FormatAll = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      facility = Some(SyslogTransformer.Rfc3164.Id.Facility),
      severity = Some(SyslogTransformer.Rfc3164.Id.Severity),
      timestamp = Some(SyslogTransformer.Rfc3164.Id.Timestamp),
      hostname = Some(SyslogTransformer.Rfc3164.Id.Hostname),
      appName = Some(SyslogTransformer.Rfc3164.Id.AppName),
      procId = Some(SyslogTransformer.Rfc3164.Id.ProcId),
      message = Some(SyslogTransformer.Rfc3164.Id.Message)
    ))
    val FormatFacility = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      facility = Some(SyslogTransformer.Rfc3164.Id.Facility)
    ))
    val FormatSeverity = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      severity = Some(SyslogTransformer.Rfc3164.Id.Severity)
    ))
    val FormatTimestamp = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      timestamp = Some(SyslogTransformer.Rfc3164.Id.Timestamp)
    ))
    val FormatHostname = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      hostname = Some(SyslogTransformer.Rfc3164.Id.Hostname)
    ))
    val FormatApp = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      appName = Some(SyslogTransformer.Rfc3164.Id.AppName)
    ))
    val FormatProc = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      procId = Some(SyslogTransformer.Rfc3164.Id.ProcId)
    ))
    val FormatMessage = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      message = Some(SyslogTransformer.Rfc3164.Id.Message)
    ))

    val OutputSimple: Json = Json.obj(
      SyslogTransformer.Rfc3164.Id.Facility -> 4,
      SyslogTransformer.Rfc3164.Id.Severity -> 2,
      SyslogTransformer.Rfc3164.Id.Timestamp -> "Aug 24 05:34:00",
      SyslogTransformer.Rfc3164.Id.Hostname -> "mymachine.example.com",
      SyslogTransformer.Rfc3164.Id.AppName -> "su",
      SyslogTransformer.Rfc3164.Id.ProcId -> "77042",
      SyslogTransformer.Rfc3164.Id.MsgId -> "ID47",
      SyslogTransformer.Rfc3164.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
    )

  }

  object Rfc5424 {

    val InputSimple = ByteString("""<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
    val InputMalformed = ByteString("""<34> 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")

    val CaptureAll = SyslogTransformer.Rfc5424.Config(binding = Binding(
      facility = Some(SyslogTransformer.Rfc5424.Id.Facility),
      severity = Some(SyslogTransformer.Rfc5424.Id.Severity),
      timestamp = Some(SyslogTransformer.Rfc5424.Id.Timestamp),
      hostname = Some(SyslogTransformer.Rfc5424.Id.Hostname),
      appName = Some(SyslogTransformer.Rfc5424.Id.AppName),
      procId = Some(SyslogTransformer.Rfc5424.Id.ProcId),
      msgId = Some(SyslogTransformer.Rfc5424.Id.MsgId),
      structData = Some(SyslogTransformer.Rfc5424.Id.StructData),
      message = Some(SyslogTransformer.Rfc5424.Id.Message)
    ))
    val CaptureFacility = SyslogTransformer.Rfc5424.Config(binding = Binding(facility = Some(SyslogTransformer.Rfc5424.Id.Facility)))
    val CaptureSeverity = SyslogTransformer.Rfc5424.Config(binding = Binding(severity = Some(SyslogTransformer.Rfc5424.Id.Severity)))
    val CaptureTimestamp = SyslogTransformer.Rfc5424.Config(binding = Binding(timestamp = Some(SyslogTransformer.Rfc5424.Id.Timestamp)))
    val CaptureHostname = SyslogTransformer.Rfc5424.Config(binding = Binding(hostname = Some(SyslogTransformer.Rfc5424.Id.Hostname)))
    val CaptureApp = SyslogTransformer.Rfc5424.Config(binding = Binding(appName = Some(SyslogTransformer.Rfc5424.Id.AppName)))
    val CaptureProc = SyslogTransformer.Rfc5424.Config(binding = Binding(procId = Some(SyslogTransformer.Rfc5424.Id.ProcId)))
    val CaptureMsg = SyslogTransformer.Rfc5424.Config(binding = Binding(msgId = Some(SyslogTransformer.Rfc5424.Id.MsgId)))
    val CaptureStructData = SyslogTransformer.Rfc5424.Config(binding = Binding(structData = Some(SyslogTransformer.Rfc5424.Id.StructData)))
    val CaptureMessage = SyslogTransformer.Rfc5424.Config(binding = Binding(message = Some(SyslogTransformer.Rfc5424.Id.Message)))

    val FormatAll = SyslogTransformer.Rfc5424.Config(binding = Binding(
      facility = Some(SyslogTransformer.Rfc5424.Id.Facility),
      severity = Some(SyslogTransformer.Rfc5424.Id.Severity),
      timestamp = Some(SyslogTransformer.Rfc5424.Id.Timestamp),
      hostname = Some(SyslogTransformer.Rfc5424.Id.Hostname),
      appName = Some(SyslogTransformer.Rfc5424.Id.AppName),
      procId = Some(SyslogTransformer.Rfc5424.Id.ProcId),
      msgId = Some(SyslogTransformer.Rfc5424.Id.MsgId),
      message = Some(SyslogTransformer.Rfc5424.Id.Message)
    ))
    val FormatFacility = SyslogTransformer.Rfc5424.Config(binding = Binding(facility = Some(SyslogTransformer.Rfc5424.Id.Facility)))
    val FormatSeverity = SyslogTransformer.Rfc5424.Config(binding = Binding(severity = Some(SyslogTransformer.Rfc5424.Id.Severity)))
    val FormatTimestamp = SyslogTransformer.Rfc5424.Config(binding = Binding(timestamp = Some(SyslogTransformer.Rfc5424.Id.Timestamp)))
    val FormatHostname = SyslogTransformer.Rfc5424.Config(binding = Binding(hostname = Some(SyslogTransformer.Rfc5424.Id.Hostname)))
    val FormatApp = SyslogTransformer.Rfc5424.Config(binding = Binding(appName = Some(SyslogTransformer.Rfc5424.Id.AppName)))
    val FormatProc = SyslogTransformer.Rfc5424.Config(binding = Binding(procId = Some(SyslogTransformer.Rfc5424.Id.ProcId)))
    val FormatMsgId = SyslogTransformer.Rfc5424.Config(binding = Binding(msgId = Some(SyslogTransformer.Rfc5424.Id.MsgId)))
    val FormatMessage = SyslogTransformer.Rfc5424.Config(binding = Binding(message = Some(SyslogTransformer.Rfc5424.Id.Message)))

    val OutputSimple: Json = Json.obj(
      SyslogTransformer.Rfc5424.Id.Facility -> 4,
      SyslogTransformer.Rfc5424.Id.Severity -> 2,
      SyslogTransformer.Rfc5424.Id.Timestamp -> "2003-10-11T22:14:15.003Z",
      SyslogTransformer.Rfc5424.Id.Hostname -> "mymachine.example.com",
      SyslogTransformer.Rfc5424.Id.AppName -> "su",
      SyslogTransformer.Rfc5424.Id.ProcId -> "77042",
      SyslogTransformer.Rfc5424.Id.MsgId -> "ID47",
      SyslogTransformer.Rfc5424.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
    )

  }


}