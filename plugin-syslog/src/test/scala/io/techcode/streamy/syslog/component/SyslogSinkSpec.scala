/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018-2021
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
import com.typesafe.config.ConfigFactory
import io.techcode.streamy.TestSystem
import io.techcode.streamy.event.StreamEvent
import pureconfig.ConfigSource

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Syslog sink spec.
  */
class SyslogSinkSpec extends TestSystem {

  import system.dispatcher

  "Syslog sink config" should {
    "be build for rfc5424" in {
      ConfigSource.fromConfig(ConfigFactory.parseString("""{"host":"127.0.0.1", "port":8888}"""))
        .loadOrThrow[SyslogSink.Rfc5424.Config]
    }

    "be build for rfc3164" in {
      ConfigSource.fromConfig(ConfigFactory.parseString("""{"host":"127.0.0.1", "port":8888}"""))
        .loadOrThrow[SyslogSink.Rfc3164.Config]
    }
  }

}



