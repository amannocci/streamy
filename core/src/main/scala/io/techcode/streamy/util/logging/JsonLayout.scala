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

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.{ILoggingEvent, IThrowableProxy, ThrowableProxyUtil}
import ch.qos.logback.core.{CoreConstants, LayoutBase}
import io.techcode.streamy.util.json._

/**
  * Json layout implementation.
  */
class JsonLayout extends LayoutBase[ILoggingEvent] {

  private var timestamp: String = "timestamp"
  private var level: String = "level"
  private var thread: String = "thread"
  private var stacktrace: String = "stacktrace"
  private var message: String = "message"

  def setTimestamp(timestamp: String): Unit = this.timestamp = timestamp

  def setLevel(level: String): Unit = this.level = level

  def setThread(thread: String): Unit = this.thread = thread

  def setStacktrace(stacktrace: String): Unit = this.stacktrace = stacktrace

  def setMessage(message: String): Unit = this.message = message

  override def doLayout(event: ILoggingEvent): String = {
    val log: JsObjectBuilder = Json.objectBuilder(event.getMDCPropertyMap.size() + 5)
    event.getMDCPropertyMap.forEach((key: String, value: String) => log += (key -> value))
    log += (level -> JsonLayout.levelToString(event.getLevel))
    log += (thread -> event.getThreadName)
    log += (timestamp -> Instant.ofEpochMilli(event.getTimeStamp).toString)
    log += (message -> event.getFormattedMessage)
    val th: IThrowableProxy = event.getThrowableProxy
    if (th != null) log += (stacktrace -> ThrowableProxyUtil.asString(th))
    log.result().toString + CoreConstants.LINE_SEPARATOR
  }

}

object JsonLayout {

  /**
    * All level convertions.
    */
  private object JsLevel {
    val Debug: Json = "debug"
    val Trace: Json = "trace"
    val Info: Json = "info"
    val Warning: Json = "warning"
    val Error: Json = "error"
  }

  /**
    * Convertion from level to string representation.
    *
    * @param level level to convert.
    */
  def levelToString(level: Level): Json = level match {
    case Level.DEBUG => JsLevel.Debug
    case Level.TRACE => JsLevel.Trace
    case Level.INFO => JsLevel.Info
    case Level.WARN => JsLevel.Warning
    case Level.ERROR => JsLevel.Error
  }

}
