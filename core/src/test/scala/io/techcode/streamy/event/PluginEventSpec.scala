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
package io.techcode.streamy.event

import io.techcode.streamy.plugin.PluginState
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

/**
  * Plugin event spec.
  */
class PluginEventSpec extends AnyWordSpecLike with Matchers {

  "Plugin event" can {
    "be converted to plugin loading state" in {
      PluginEvent.Loading("foobar").toState should equal(PluginState.Loading)
    }

    "be converted to plugin running state" in {
      PluginEvent.Running("foobar").toState should equal(PluginState.Running)
    }

    "be converted to plugin stopping state" in {
      PluginEvent.Stopping("foobar").toState should equal(PluginState.Stopping)
    }

    "be converted to plugin stopped state" in {
      PluginEvent.Stopped("foobar").toState should equal(PluginState.Stopped)
    }

    "be converted to string" in {
      PluginEvent.Stopped("foobar").toString should equal("Stopped")
    }
  }

}
