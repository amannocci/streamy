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
package io.techcode.streamy.util.logging

import java.time.Instant

import ch.qos.logback.classic.spi.{ILoggingEvent, IThrowableProxy, ThrowableProxyUtil}
import ch.qos.logback.core.{CoreConstants, LayoutBase}
import io.techcode.streamy.util.json.{JsObjectBuilder, Json}

/**
  * Json layout implementation.
  */
class JsonLayout extends LayoutBase[ILoggingEvent] {

  private var `type`: Option[String] = None

  def setType(`type`: String): Unit = {
    this.`type` = Some(`type`)
  }

  override def doLayout(event: ILoggingEvent): String = {
    val log: JsObjectBuilder = Json.objectBuilder()
    `type`.foreach(log.put(JsonLayout.LogType, _))
    log.put(JsonLayout.LogLevel, event.getLevel.toString)
    log.put(JsonLayout.LogThread, event.getThreadName)
    log.put(JsonLayout.LogTimestamp, Instant.ofEpochMilli(event.getTimeStamp).toString)
    event.getMDCPropertyMap.forEach { (key: String, value: String) =>
      log.put(key, value)
    }
    log.put(JsonLayout.LogMessage, event.getFormattedMessage)
    val th: IThrowableProxy = event.getThrowableProxy
    if (th != null) log.put(JsonLayout.LogStacktrace, ThrowableProxyUtil.asString(th))
    log.result().toString + CoreConstants.LINE_SEPARATOR
  }

}

object JsonLayout {

  // Some constant
  val LogStacktrace = "stacktrace"
  val LogLevel = "level"
  val LogThread = "thread"
  val LogTimestamp = "timestamp"
  val LogType = "type"
  val LogMessage = "message"

}
