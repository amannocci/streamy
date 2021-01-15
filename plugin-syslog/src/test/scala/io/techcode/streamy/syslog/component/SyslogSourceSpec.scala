/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018
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
package io.techcode.streamy.syslog.component

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.techcode.streamy.TestSystem
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.tcp.component.TcpSink
import io.techcode.streamy.util.json.Json

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Syslog source spec.
  */
class SyslogSourceSpec extends TestSystem {

  import system.dispatcher

  "Syslog source" should {
    "emit stream event rfc5424 correctly from multiples connections" in {
      val runnable = SyslogSource.server(SyslogSourceSpec.Source.Rfc5424)
        .toMat(TestSink.probe[StreamEvent])(Keep.both)
        .run()

      runnable._1.onComplete {
        case Success(_) =>
          Source.single(SyslogSourceSpec.Sample.Rfc5424)
            .runWith(SyslogSink.client(SyslogSourceSpec.Sink.Rfc5424))
          Source.single(SyslogSourceSpec.Sample.Rfc5424)
            .runWith(SyslogSink.client(SyslogSourceSpec.Sink.Rfc5424))
        case Failure(ex) => ex.printStackTrace()
      }

      runnable._2.requestNext(5 seconds) should equal(SyslogSourceSpec.Sample.Rfc5424)
      runnable._2.requestNext(5 seconds) should equal(SyslogSourceSpec.Sample.Rfc5424)
    }

    "emit stream event rfc3164 correctly from multiples connections" in {
      val runnable = SyslogSource.server(SyslogSourceSpec.Source.Rfc3164)
        .toMat(TestSink.probe[StreamEvent])(Keep.both)
        .run()

      runnable._1.onComplete {
        case Success(_) =>
          Source.single(SyslogSourceSpec.Sample.Rfc3164)
            .runWith(SyslogSink.client(SyslogSourceSpec.Sink.Rfc3164))
          Source.single(SyslogSourceSpec.Sample.Rfc3164)
            .runWith(SyslogSink.client(SyslogSourceSpec.Sink.Rfc3164))
        case Failure(ex) => ex.printStackTrace()
      }

      runnable._2.requestNext(5 seconds) should equal(SyslogSourceSpec.Sample.Rfc3164)
      runnable._2.requestNext(5 seconds) should equal(SyslogSourceSpec.Sample.Rfc3164)
    }
  }

}

object SyslogSourceSpec {

  object Sample {
    val Rfc5424: StreamEvent = StreamEvent(Json.obj(
      SyslogTransformer.Rfc5424.Id.Facility -> 4,
      SyslogTransformer.Rfc5424.Id.Severity -> 2,
      SyslogTransformer.Rfc5424.Id.Timestamp -> "2003-10-11T22:14:15.003Z",
      SyslogTransformer.Rfc5424.Id.Hostname -> "mymachine.example.com",
      SyslogTransformer.Rfc5424.Id.AppName -> "su",
      SyslogTransformer.Rfc5424.Id.ProcId -> "77042",
      SyslogTransformer.Rfc5424.Id.MsgId -> "ID47",
      SyslogTransformer.Rfc5424.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
    ))

    val Rfc3164: StreamEvent = StreamEvent(Json.obj(
      SyslogTransformer.Rfc3164.Id.Facility -> 4,
      SyslogTransformer.Rfc3164.Id.Severity -> 2,
      SyslogTransformer.Rfc3164.Id.Timestamp -> "Apr  4 13:51:20",
      SyslogTransformer.Rfc3164.Id.Hostname -> "mymachine.example.com",
      SyslogTransformer.Rfc3164.Id.AppName -> "su",
      SyslogTransformer.Rfc3164.Id.ProcId -> "77042",
      SyslogTransformer.Rfc3164.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
    ))
  }

  object Source {

    val Rfc5424: SyslogSource.Rfc5424.Config = SyslogSource.Rfc5424.Config(
      host = "localhost",
      port = 8080,
      binding = SyslogTransformerSpec.Rfc5424.Transformer.Binding
    )

    val Rfc3164: SyslogSource.Rfc3164.Config = SyslogSource.Rfc3164.Config(
      host = "localhost",
      port = 8081,
      binding = SyslogTransformerSpec.Rfc3164.Transformer.Binding
    )

  }

  object Sink {

    val Rfc5424: SyslogSink.Rfc5424.Config = SyslogSink.Rfc5424.Config(
      host = "localhost",
      port = 8080,
      binding = SyslogTransformerSpec.Rfc5424.Transformer.Binding
    )

    val Rfc3164: SyslogSink.Rfc3164.Config = SyslogSink.Rfc3164.Config(
      host = "localhost",
      port = 8081,
      binding = SyslogTransformerSpec.Rfc3164.Transformer.Binding
    )

  }

}

