/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2018
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
package io.techcode.streamy.syslog.component.transformer

import java.net.InetAddress
import java.nio.charset.StandardCharsets

import akka.util.ByteString
import io.techcode.streamy.component.TestTransformer
import io.techcode.streamy.syslog.component.transformer.SyslogTransformer.Rfc5424.Mode
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.{IntBinder, StringBinder}

/**
  * Syslog transformer spec.
  */
class SyslogTransformerSpec extends TestTransformer {

  "Syslog transformer" should {
    "in" should {
      "Rfc5424" should {
        "handle correctly simple syslog message in strict mode" in {
          except(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureAllStrict),
            SyslogTransformerSpec.Rfc5424.InputSimple,
            Json.obj(
              SyslogTransformer.Rfc5424.Id.Facility -> 4,
              SyslogTransformer.Rfc5424.Id.Severity -> 2,
              SyslogTransformer.Rfc5424.Id.Timestamp -> "2003-10-11T22:14:15.003Z",
              SyslogTransformer.Rfc5424.Id.Hostname -> "mymachine.example.com",
              SyslogTransformer.Rfc5424.Id.AppName -> "su",
              SyslogTransformer.Rfc5424.Id.ProcId -> "77042",
              SyslogTransformer.Rfc5424.Id.MsgId -> "ID47",
              SyslogTransformer.Rfc5424.Id.StructData -> """[sigSig ver="1"]""",
              SyslogTransformer.Rfc5424.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
            )
          )
        }

        "handle correctly simple syslog message in lenient mode" in {
          except(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureAllLenient),
            SyslogTransformerSpec.Rfc5424.InputSimple,
            Json.obj(
              SyslogTransformer.Rfc5424.Id.Facility -> 4,
              SyslogTransformer.Rfc5424.Id.Severity -> 2,
              SyslogTransformer.Rfc5424.Id.Timestamp -> "2003-10-11T22:14:15.003Z",
              SyslogTransformer.Rfc5424.Id.Hostname -> "mymachine.example.com",
              SyslogTransformer.Rfc5424.Id.AppName -> "su",
              SyslogTransformer.Rfc5424.Id.ProcId -> "77042",
              SyslogTransformer.Rfc5424.Id.MsgId -> "ID47",
              SyslogTransformer.Rfc5424.Id.StructData -> """[sigSig ver="1"]""",
              SyslogTransformer.Rfc5424.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
            )
          )
        }

        "capture only facility when set" in {
          except(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureFacility),
            SyslogTransformerSpec.Rfc5424.InputSimple,
            Json.obj(SyslogTransformer.Rfc5424.Id.Facility -> 4)
          )
        }

        "capture only severity when set" in {
          except(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureSeverity),
            SyslogTransformerSpec.Rfc5424.InputSimple,
            Json.obj(SyslogTransformer.Rfc5424.Id.Severity -> 2)
          )
        }

        "capture only timestamp when set" in {
          except(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureTimestamp),
            SyslogTransformerSpec.Rfc5424.InputSimple,
            Json.obj(SyslogTransformer.Rfc5424.Id.Timestamp -> "2003-10-11T22:14:15.003Z")
          )
        }

        "capture only alternative timestamp when set" in {
          except(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureTimestamp),
            SyslogTransformerSpec.Rfc5424.InputAlternative,
            Json.obj(SyslogTransformer.Rfc5424.Id.Timestamp -> "1985-04-12T19:20:50.52-04:00")
          )
        }

        "capture only hostname when set" in {
          except(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureHostname),
            SyslogTransformerSpec.Rfc5424.InputSimple,
            Json.obj(SyslogTransformer.Rfc5424.Id.Hostname -> "mymachine.example.com")
          )
        }

        "capture only app when set" in {
          except(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureApp),
            SyslogTransformerSpec.Rfc5424.InputSimple,
            Json.obj(SyslogTransformer.Rfc5424.Id.AppName -> "su")
          )
        }

        "capture only proc when set" in {
          except(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureProc),
            SyslogTransformerSpec.Rfc5424.InputSimple,
            Json.obj(SyslogTransformer.Rfc5424.Id.ProcId -> "77042")
          )
        }

        "capture only msg when set" in {
          except(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureMsg),
            SyslogTransformerSpec.Rfc5424.InputSimple,
            Json.obj(SyslogTransformer.Rfc5424.Id.MsgId -> "ID47")
          )
        }

        "capture only struct data when set" in {
          except(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureStructData),
            SyslogTransformerSpec.Rfc5424.InputSimple,
            Json.obj(SyslogTransformer.Rfc5424.Id.StructData -> """[sigSig ver="1"]""")
          )
        }

        "capture only message when set" in {
          except(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureMessage),
            SyslogTransformerSpec.Rfc5424.InputSimple,
            Json.obj(SyslogTransformer.Rfc5424.Id.Message -> "'su root' failed for lonvick on /dev/pts/8")
          )
        }

        "throw an error when syslog message is malformed" in {
          exceptError(
            SyslogTransformer.inRfc5424(SyslogTransformerSpec.Rfc5424.CaptureAllLenient),
            SyslogTransformerSpec.Rfc5424.InputMalformed
          )
        }
      }
    }

    "out" should {
      "Rfc3164" should {
        "format correctly simple syslog message" in {
          except(
            SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatAll),
            SyslogTransformerSpec.Rfc3164.OutputSimple,
            ByteString("<34>Aug 24 05:34:00 mymachine.example.com su[77042]: 'su root' failed for lonvick on /dev/pts/8\n")
          )
        }

        "format correctly when only facility is set" in {
          except(
            SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatFacility),
            SyslogTransformerSpec.Rfc3164.OutputSimple,
            ByteString(s"<38>Jan 1 00:00:00.000 ${SyslogTransformerSpec.Hostname} streamy[1]\n")
          )
        }

        "format correctly when only severity is set" in {
          except(
            SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatSeverity),
            SyslogTransformerSpec.Rfc3164.OutputSimple,
            ByteString(s"<26>Jan 1 00:00:00.000 ${SyslogTransformerSpec.Hostname} streamy[1]\n")
          )
        }

        "format correctly when only timestamp is set" in {
          except(
            SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatTimestamp),
            SyslogTransformerSpec.Rfc3164.OutputSimple,
            ByteString(s"<30>Aug 24 05:34:00 ${SyslogTransformerSpec.Hostname} streamy[1]\n")
          )
        }

        "format correctly when only hostname is set" in {
          except(
            SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatHostname),
            SyslogTransformerSpec.Rfc3164.OutputSimple,
            ByteString("<30>Jan 1 00:00:00.000 mymachine.example.com streamy[1]\n")
          )
        }

        "format correctly when only app is set" in {
          except(
            SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatApp),
            SyslogTransformerSpec.Rfc3164.OutputSimple,
            ByteString(s"<30>Jan 1 00:00:00.000 ${SyslogTransformerSpec.Hostname} su[1]\n")
          )
        }

        "format correctly when only proc is set" in {
          except(
            SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatProc),
            SyslogTransformerSpec.Rfc3164.OutputSimple,
            ByteString(s"<30>Jan 1 00:00:00.000 ${SyslogTransformerSpec.Hostname} streamy[77042]\n")
          )
        }

        "format correctly when only message is set" in {
          except(
            SyslogTransformer.outRfc3164(SyslogTransformerSpec.Rfc3164.FormatMessage),
            SyslogTransformerSpec.Rfc3164.OutputSimple,
            ByteString(s"<30>Jan 1 00:00:00.000 ${SyslogTransformerSpec.Hostname} streamy[1]: 'su root' failed for lonvick on /dev/pts/8\n")
          )
        }
      }

      "Rfc5424" should {
        "format correctly simple syslog message" in {
          except(
            SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatAll),
            SyslogTransformerSpec.Rfc5424.OutputSimple,
            ByteString("<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 - 'su root' failed for lonvick on /dev/pts/8")
          )
        }

        "format correctly when only facility is set" in {
          except(
            SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatFacility),
            SyslogTransformerSpec.Rfc5424.OutputSimple,
            ByteString("<38>1 1970-01-01T00:00:00.000Z - - - - -")
          )
        }

        "format correctly when only severity is set" in {
          except(
            SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatSeverity),
            SyslogTransformerSpec.Rfc5424.OutputSimple,
            ByteString("<26>1 1970-01-01T00:00:00.000Z - - - - -")
          )
        }

        "format correctly when only timestamp is set" in {
          except(
            SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatTimestamp),
            SyslogTransformerSpec.Rfc5424.OutputSimple,
            ByteString("<30>1 2003-10-11T22:14:15.003Z - - - - -")
          )
        }

        "format correctly when only hostname is set" in {
          except(
            SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatHostname),
            SyslogTransformerSpec.Rfc5424.OutputSimple,
            ByteString("<30>1 1970-01-01T00:00:00.000Z mymachine.example.com - - - -")
          )
        }

        "format correctly when only app is set" in {
          except(
            SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatApp),
            SyslogTransformerSpec.Rfc5424.OutputSimple,
            ByteString("<30>1 1970-01-01T00:00:00.000Z - su - - -")
          )
        }

        "format correctly when only proc is set" in {
          except(
            SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatProc),
            SyslogTransformerSpec.Rfc5424.OutputSimple,
            ByteString("<30>1 1970-01-01T00:00:00.000Z - - 77042 - -")
          )
        }

        "format correctly when only msgId is set" in {
          except(
            SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatMsgId),
            SyslogTransformerSpec.Rfc5424.OutputSimple,
            ByteString("<30>1 1970-01-01T00:00:00.000Z - - - ID47 -")
          )
        }

        "format correctly when only message is set" in {
          except(
            SyslogTransformer.outRfc5424(SyslogTransformerSpec.Rfc5424.FormatMessage),
            SyslogTransformerSpec.Rfc5424.OutputSimple,
            ByteString("<30>1 1970-01-01T00:00:00.000Z - - - - - 'su root' failed for lonvick on /dev/pts/8")
          )
        }
      }
    }
  }

}

object SyslogTransformerSpec {

  val Hostname: String = InetAddress.getLocalHost.getHostName

  object Rfc3164 {

    val FormatAll = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      facility = Some(IntBinder(SyslogTransformer.Rfc3164.Id.Facility)),
      severity = Some(IntBinder(SyslogTransformer.Rfc3164.Id.Severity)),
      timestamp = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Timestamp, StandardCharsets.US_ASCII)),
      hostname = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Hostname, StandardCharsets.US_ASCII)),
      appName = Some(StringBinder(SyslogTransformer.Rfc3164.Id.AppName, StandardCharsets.US_ASCII)),
      procId = Some(StringBinder(SyslogTransformer.Rfc3164.Id.ProcId, StandardCharsets.US_ASCII)),
      message = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Message))
    ))
    val FormatFacility = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      facility = Some(IntBinder(SyslogTransformer.Rfc3164.Id.Facility))
    ))
    val FormatSeverity = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      severity = Some(IntBinder(SyslogTransformer.Rfc3164.Id.Severity))
    ))
    val FormatTimestamp = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      timestamp = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Timestamp, StandardCharsets.US_ASCII))
    ))
    val FormatHostname = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      hostname = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Hostname, StandardCharsets.US_ASCII))
    ))
    val FormatApp = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      appName = Some(StringBinder(SyslogTransformer.Rfc3164.Id.AppName, StandardCharsets.US_ASCII))
    ))
    val FormatProc = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      procId = Some(StringBinder(SyslogTransformer.Rfc3164.Id.ProcId, StandardCharsets.US_ASCII))
    ))
    val FormatMessage = SyslogTransformer.Rfc3164.Config(binding = SyslogTransformer.Rfc3164.Binding(
      message = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Message))
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
    val InputAlternative = ByteString("""<34>1 1985-04-12T19:20:50.52-04:00 mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
    val InputMalformed = ByteString("""<34> 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")

    val CaptureAllStrict = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      facility = Some(IntBinder(SyslogTransformer.Rfc5424.Id.Facility)),
      severity = Some(IntBinder(SyslogTransformer.Rfc5424.Id.Severity)),
      timestamp = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Timestamp, StandardCharsets.US_ASCII)),
      hostname = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Hostname, StandardCharsets.US_ASCII)),
      appName = Some(StringBinder(SyslogTransformer.Rfc5424.Id.AppName, StandardCharsets.US_ASCII)),
      procId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.ProcId, StandardCharsets.US_ASCII)),
      msgId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.MsgId, StandardCharsets.US_ASCII)),
      structData = Some(StringBinder(SyslogTransformer.Rfc5424.Id.StructData, StandardCharsets.US_ASCII)),
      message = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Message))
    ))

    val CaptureAllLenient = SyslogTransformer.Rfc5424.Config(
      mode = Mode.Lenient,
      binding = SyslogTransformer.Rfc5424.Binding(
        facility = Some(IntBinder(SyslogTransformer.Rfc5424.Id.Facility)),
        severity = Some(IntBinder(SyslogTransformer.Rfc5424.Id.Severity)),
        timestamp = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Timestamp, StandardCharsets.US_ASCII)),
        hostname = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Hostname, StandardCharsets.US_ASCII)),
        appName = Some(StringBinder(SyslogTransformer.Rfc5424.Id.AppName, StandardCharsets.US_ASCII)),
        procId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.ProcId, StandardCharsets.US_ASCII)),
        msgId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.MsgId, StandardCharsets.US_ASCII)),
        structData = Some(StringBinder(SyslogTransformer.Rfc5424.Id.StructData, StandardCharsets.US_ASCII)),
        message = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Message))
      ))
    val CaptureFacility = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      facility = Some(IntBinder(SyslogTransformer.Rfc5424.Id.Facility))
    ))
    val CaptureSeverity = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      severity = Some(IntBinder(SyslogTransformer.Rfc5424.Id.Severity))
    ))
    val CaptureTimestamp = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      timestamp = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Timestamp, StandardCharsets.US_ASCII))
    ))
    val CaptureHostname = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      hostname = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Hostname, StandardCharsets.US_ASCII))
    ))
    val CaptureApp = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      appName = Some(StringBinder(SyslogTransformer.Rfc5424.Id.AppName, StandardCharsets.US_ASCII))
    ))
    val CaptureProc = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      procId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.ProcId, StandardCharsets.US_ASCII))
    ))
    val CaptureMsg = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      msgId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.MsgId, StandardCharsets.US_ASCII))
    ))
    val CaptureStructData = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      structData = Some(StringBinder(SyslogTransformer.Rfc5424.Id.StructData, StandardCharsets.US_ASCII))
    ))
    val CaptureMessage = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      message = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Message))
    ))

    val FormatAll = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      facility = Some(IntBinder(SyslogTransformer.Rfc5424.Id.Facility)),
      severity = Some(IntBinder(SyslogTransformer.Rfc5424.Id.Severity)),
      timestamp = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Timestamp, StandardCharsets.US_ASCII)),
      hostname = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Hostname, StandardCharsets.US_ASCII)),
      appName = Some(StringBinder(SyslogTransformer.Rfc5424.Id.AppName, StandardCharsets.US_ASCII)),
      procId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.ProcId, StandardCharsets.US_ASCII)),
      msgId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.MsgId, StandardCharsets.US_ASCII)),
      message = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Message))
    ))
    val FormatFacility = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      facility = Some(IntBinder(SyslogTransformer.Rfc5424.Id.Facility))
    ))
    val FormatSeverity = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      severity = Some(IntBinder(SyslogTransformer.Rfc5424.Id.Severity))
    ))
    val FormatTimestamp = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      timestamp = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Timestamp, StandardCharsets.US_ASCII))
    ))
    val FormatHostname = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      hostname = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Hostname, StandardCharsets.US_ASCII))
    ))
    val FormatApp = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      appName = Some(StringBinder(SyslogTransformer.Rfc5424.Id.AppName, StandardCharsets.US_ASCII))
    ))
    val FormatProc = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      procId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.ProcId, StandardCharsets.US_ASCII))
    ))
    val FormatMsgId = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      msgId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.MsgId, StandardCharsets.US_ASCII))
    ))
    val FormatMessage = SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      message = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Message))
    ))

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