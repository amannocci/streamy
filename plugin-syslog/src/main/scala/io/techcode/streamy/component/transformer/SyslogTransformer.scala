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

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import io.techcode.streamy.util.json.Json

object SyslogTransformer {

  /**
    * Create a syslog input transformer flow that transform incoming [[ByteString]] to [[Json]].
    * This flow is RFC5424 compliant.
    *
    * @param conf flow configuration.
    * @return new syslog flow RCF5424 compliant.
    */
  def rfc5424(conf: SyslogInput.RFC5424Config): Flow[ByteString, Json, NotUsed] =
    Flow.fromFunction(new SyslogRFC5424Input(conf))

  /**
    * Create a syslog output transformer flow that transform incoming [[Json]] to [[ByteString]].
    * This flow is RFC5424 compliant.
    *
    * @param conf flow configuration.
    * @return new syslog flow RCF5424 compliant.
    */
  def rfc5424(conf: SyslogOutput.RFC5424Config): Flow[Json, ByteString, NotUsed] =
    Flow.fromFunction(new SyslogRFC5424Output(conf))

  /**
    * Create a syslog output transformer RCF3126 compilant.
    *
    * @param conf output configuration.
    * @return new syslog flow RCF3126 compliant.
    */
  def rfc3164(conf: SyslogOutput.RFC3164Config): Flow[Json, ByteString, NotUsed] =
    Flow.fromFunction(new SyslogRFC3164Output(conf))

}
