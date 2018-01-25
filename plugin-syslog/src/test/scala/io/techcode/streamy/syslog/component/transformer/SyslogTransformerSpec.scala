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

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import io.techcode.streamy.component.TestTransformer
import io.techcode.streamy.syslog.component.transformer.SyslogTransformer.Framing
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
        "with delimiter" should {
          "handle correctly simple syslog message in strict mode" in {
            except(
              SyslogTransformerSpec.Rfc5424.Transformer.InStrictDelimiter,
              SyslogTransformerSpec.Rfc5424.Input.InSimpleDelimiter,
              SyslogTransformerSpec.Rfc5424.Output.InSimple
            )
          }

          "handle correctly simple syslog message in lenient mode" in {
            except(
              SyslogTransformerSpec.Rfc5424.Transformer.InLenientDelimiter,
              SyslogTransformerSpec.Rfc5424.Input.InSimpleDelimiter,
              SyslogTransformerSpec.Rfc5424.Output.InSimple
            )
          }

          "handle correctly alternative syslog message" in {
            except(
              SyslogTransformerSpec.Rfc5424.Transformer.InLenientDelimiter,
              SyslogTransformerSpec.Rfc5424.Input.InAlternativeDelimiter,
              SyslogTransformerSpec.Rfc5424.Output.InAlternative
            )
          }

          "throw an error when syslog message is malformed" in {
            exceptError(
              SyslogTransformerSpec.Rfc5424.Transformer.InLenientDelimiter,
              SyslogTransformerSpec.Rfc5424.Input.InMalformedDelimiter
            )
          }
        }

        "with count" should {
          "handle correctly simple syslog message in strict mode" in {
            except(
              SyslogTransformerSpec.Rfc5424.Transformer.InStrictCount,
              SyslogTransformerSpec.Rfc5424.Input.InSimpleCount,
              SyslogTransformerSpec.Rfc5424.Output.InSimple
            )
          }

          "handle correctly simple syslog message in lenient mode" in {
            except(
              SyslogTransformerSpec.Rfc5424.Transformer.InLenientCount,
              SyslogTransformerSpec.Rfc5424.Input.InSimpleCount,
              SyslogTransformerSpec.Rfc5424.Output.InSimple
            )
          }

          "handle correctly alternative syslog message" in {
            except(
              SyslogTransformerSpec.Rfc5424.Transformer.InLenientCount,
              SyslogTransformerSpec.Rfc5424.Input.InAlternativeCount,
              SyslogTransformerSpec.Rfc5424.Output.InAlternative
            )
          }

          "throw an error when syslog message is malformed" in {
            exceptError(
              SyslogTransformerSpec.Rfc5424.Transformer.InLenientCount,
              SyslogTransformerSpec.Rfc5424.Input.InMalformedCount
            )
          }
        }
      }
    }

    "out" should {
      "Rfc3164" should {
        "with frame delimiter" in {
          except(
            SyslogTransformerSpec.Rfc3164.Transformer.OutDelimiter,
            SyslogTransformerSpec.Rfc3164.Input.OutSimple,
            SyslogTransformerSpec.Rfc3164.Output.OutSimpleDelimiter
          )
        }

        "with frame count" in {
          except(
            SyslogTransformerSpec.Rfc3164.Transformer.OutCount,
            SyslogTransformerSpec.Rfc3164.Input.OutSimple,
            SyslogTransformerSpec.Rfc3164.Output.OutSimpleCount
          )
        }
      }

      "Rfc5424" should {
        "with frame delimiter" in {
          except(
            SyslogTransformerSpec.Rfc5424.Transformer.OutDelimiter,
            SyslogTransformerSpec.Rfc5424.Input.OutSimple,
            SyslogTransformerSpec.Rfc5424.Output.OutSimpleDelimiter
          )
        }

        "with frame count" in {
          except(
            SyslogTransformerSpec.Rfc5424.Transformer.OutCount,
            SyslogTransformerSpec.Rfc5424.Input.OutSimple,
            SyslogTransformerSpec.Rfc5424.Output.OutSimpleCount
          )
        }
      }
    }
  }

}

object SyslogTransformerSpec {

  val Hostname: String = InetAddress.getLocalHost.getHostName

  def framingDelimiter(data: ByteString): ByteString = {
    data ++ ByteString('\n')
  }

  def framingCount(data: ByteString): ByteString = {
    val count = ByteString(data.length.toString)
    count ++ ByteString(' ') ++ data
  }

  object Rfc3164 {

    object Input {

      val OutSimple: Json = Json.obj(
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

    object Transformer {
      val Binding = SyslogTransformer.Rfc3164.Binding(
        facility = Some(IntBinder(SyslogTransformer.Rfc3164.Id.Facility)),
        severity = Some(IntBinder(SyslogTransformer.Rfc3164.Id.Severity)),
        timestamp = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Timestamp, StandardCharsets.US_ASCII)),
        hostname = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Hostname, StandardCharsets.US_ASCII)),
        appName = Some(StringBinder(SyslogTransformer.Rfc3164.Id.AppName, StandardCharsets.US_ASCII)),
        procId = Some(StringBinder(SyslogTransformer.Rfc3164.Id.ProcId, StandardCharsets.US_ASCII)),
        message = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Message))
      )

      val OutDelimiter: Flow[Json, ByteString, NotUsed] = SyslogTransformer.outRfc3164(SyslogTransformer.Rfc3164.Config(
        binding = Binding
      ))

      val OutCount: Flow[Json, ByteString, NotUsed] = SyslogTransformer.outRfc3164(SyslogTransformer.Rfc3164.Config(
        framing = Framing.Count,
        binding = Binding
      ))

    }

    object Output {

      val OutSimple: ByteString = ByteString("<34>Aug 24 05:34:00 mymachine.example.com su[77042]: 'su root' failed for lonvick on /dev/pts/8")

      val OutSimpleDelimiter: ByteString = framingDelimiter(OutSimple)

      val OutSimpleCount: ByteString = framingCount(OutSimple)

    }

  }

  object Rfc5424 {

    object Input {

      val InSimple: ByteString = ByteString("""<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
      val InSimpleDelimiter: ByteString = framingDelimiter(InSimple)
      val InSimpleCount: ByteString = framingCount(InSimple)

      val InAlternative: ByteString = ByteString("""<34>1 1985-04-12T19:20:50.52-04:00 mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
      val InAlternativeDelimiter: ByteString = framingDelimiter(InAlternative)
      val InAlternativeCount: ByteString = framingCount(InAlternative)

      val InMalformed: ByteString = ByteString("""<34> 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
      val InMalformedDelimiter: ByteString = framingDelimiter(InMalformed)
      val InMalformedCount: ByteString = framingCount(InMalformed)

      val OutSimple: Json = Json.obj(
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

    object Transformer {

      private val Binding = SyslogTransformer.Rfc5424.Binding(
        facility = Some(IntBinder(SyslogTransformer.Rfc5424.Id.Facility)),
        severity = Some(IntBinder(SyslogTransformer.Rfc5424.Id.Severity)),
        timestamp = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Timestamp, StandardCharsets.US_ASCII)),
        hostname = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Hostname, StandardCharsets.US_ASCII)),
        appName = Some(StringBinder(SyslogTransformer.Rfc5424.Id.AppName, StandardCharsets.US_ASCII)),
        procId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.ProcId, StandardCharsets.US_ASCII)),
        msgId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.MsgId, StandardCharsets.US_ASCII)),
        structData = Some(StringBinder(SyslogTransformer.Rfc5424.Id.StructData, StandardCharsets.US_ASCII)),
        message = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Message))
      )

      val InStrictDelimiter: Flow[ByteString, Json, NotUsed] = SyslogTransformer.inRfc5424(SyslogTransformer.Rfc5424.Config(
        binding = Binding
      ))

      val InLenientDelimiter: Flow[ByteString, Json, NotUsed] = SyslogTransformer.inRfc5424(SyslogTransformer.Rfc5424.Config(
        mode = Mode.Lenient,
        binding = Binding
      ))

      val InStrictCount: Flow[ByteString, Json, NotUsed] = SyslogTransformer.inRfc5424(SyslogTransformer.Rfc5424.Config(
        framing = Framing.Count,
        binding = Binding
      ))

      val InLenientCount: Flow[ByteString, Json, NotUsed] = SyslogTransformer.inRfc5424(SyslogTransformer.Rfc5424.Config(
        mode = Mode.Lenient,
        framing = Framing.Count,
        binding = Binding
      ))

      val OutDelimiter: Flow[Json, ByteString, NotUsed] = SyslogTransformer.outRfc5424(SyslogTransformer.Rfc5424.Config(
        binding = Binding
      ))

      val OutCount: Flow[Json, ByteString, NotUsed] = SyslogTransformer.outRfc5424(SyslogTransformer.Rfc5424.Config(
        framing = Framing.Count,
        binding = Binding
      ))

    }

    object Output {

      val InSimple: Json = Json.obj(
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

      val InAlternative: Json = InSimple.patch(Replace(Root / SyslogTransformer.Rfc5424.Id.Timestamp, "1985-04-12T19:20:50.52-04:00")).get

      val OutSimple: ByteString = ByteString("<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 - 'su root' failed for lonvick on /dev/pts/8")

      val OutSimpleDelimiter: ByteString = framingDelimiter(OutSimple)

      val OutSimpleCount: ByteString = framingCount(OutSimple)

    }

  }


}