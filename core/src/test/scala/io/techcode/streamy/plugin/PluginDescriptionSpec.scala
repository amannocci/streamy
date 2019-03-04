/*
 * The MIT License (MIT)
 * <p>
 * Copyright (C) 2017-2019
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
package io.techcode.streamy.plugin

import com.typesafe.config.ConfigFactory
import org.scalatest._
import pureconfig._
import pureconfig.generic.auto._

import scala.reflect.io.Path

/**
  * Plugin description spec.
  */
class PluginDescriptionSpec extends WordSpec with Matchers with Inside with PrivateMethodTester {

  "PluginDescription" should {
    "contains all informations" in {
      val description = PluginDescription(name = "test", version = "1.0.0", file = Some(Path(".").toURL))
      inside(description) { case PluginDescription(name, version, _, _, _, _, _) =>
        name should be("test")
        version should be("1.0.0")
      }
    }

    "be create from Config" in {
      loadConfigOrThrow[PluginDescription](ConfigFactory.parseString("""{"name":"test","version":"0.1.0"}"""))
    }
  }

}