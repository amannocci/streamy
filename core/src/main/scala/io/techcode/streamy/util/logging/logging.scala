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
package io.techcode.streamy.util

import akka.event.LoggingAdapter
import org.slf4j.MDC

package object logging {

  /**
    * Logging adapter wrapper for proper logging.
    *
    * @param underlying logging adapter.
    */
  implicit class RichLogging(val underlying: LoggingAdapter) extends AnyVal {

    /**
      * Create a new context cleaning mdc at the end.
      *
      * @param inner inner logging statement.
      */
    def withContext(inner: => Unit): Unit = try {
      inner
    } finally {
      MDC.clear()
    }

    /**
      * Clean shortcut to fill mdc.
      *
      * @param key   key element.
      * @param value value of element.
      */
    @inline def putMDC(key: String, value: String): Unit = MDC.put(key, value)

  }

}
