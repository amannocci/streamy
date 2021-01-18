/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2021
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
package io.techcode.streamy.syslog

import io.techcode.streamy.component.ComponentRegistry
import io.techcode.streamy.plugin.Plugin
import io.techcode.streamy.syslog.component.{SyslogSink, SyslogSource}
import pureconfig._

/**
  * Syslog plugin implementation.
  */
class SyslogPlugin(
  data: Plugin.Data
) extends Plugin(data) {

  override def onStart(): Unit = {
    val componentRegistry = ComponentRegistry(system)
    componentRegistry.registerSource("syslog-rfc5424", conf => {
      SyslogSource.server(ConfigSource.fromConfig(conf).loadOrThrow[SyslogSource.Rfc5424.Config])
    })
    componentRegistry.registerSource("syslog-rfc3164", conf => {
      SyslogSource.server(ConfigSource.fromConfig(conf).loadOrThrow[SyslogSource.Rfc3164.Config])
    })
    componentRegistry.registerSink("syslog-rfc5424", conf => {
      SyslogSink.client(ConfigSource.fromConfig(conf).loadOrThrow[SyslogSink.Rfc5424.Config])
    })
    componentRegistry.registerSink("syslog-rfc3164", conf => {
      SyslogSink.client(ConfigSource.fromConfig(conf).loadOrThrow[SyslogSink.Rfc3164.Config])
    })
  }

}
