/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2020
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
package io.techcode.streamy.util.lang

import io.techcode.streamy.StreamyTestSystem
import io.techcode.streamy.util.lang.SystemAccess.Platform

/**
  * System access spec.
  */
class SystemAccessSpec extends StreamyTestSystem {

  "System access" should {
    "return correct reflected method if available" in {
      SystemAccess.getMethod("getProcessCpuTime").isDefined should equal(true)
    }

    "return none for reflected method if unavailable" in {
      SystemAccess.getMethod("foobar").isEmpty should equal(true)
    }

    "return correct unix reflected method if available" in {
      SystemAccess.getUnixMethod("getOpenFileDescriptorCount").isDefined should equal(true)
    }

    "return none for unix reflected method if unavailable" in {
      SystemAccess.getUnixMethod("foobar").isEmpty should equal(true)
    }

    "detect correctly windows platform" in {
      SystemAccess.detectPlatform("windows") should equal(Platform.Windows)
    }

    "detect correctly macos platform" in {
      SystemAccess.detectPlatform("mac") should equal(Platform.MacOS)
    }

    "detect correctly linux platform" in {
      SystemAccess.detectPlatform("linux") should equal(Platform.Linux)
    }

    "return correct platform statement" in {
      if (SystemAccess.isLinux) {
        SystemAccess.isMacOS should equal(false)
        SystemAccess.isWindows should equal(false)
      } else if (SystemAccess.isWindows) {
        SystemAccess.isLinux should equal(false)
        SystemAccess.isMacOS should equal(false)
      } else {
        SystemAccess.isLinux should equal(false)
        SystemAccess.isWindows should equal(false)
      }
    }
  }

}
