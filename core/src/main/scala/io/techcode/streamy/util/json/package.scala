/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2018
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

import akka.util.ByteString

package object json extends JsonImplicit {

  // Root json pointer
  val Root: JsonPointer = JsonPointer()

  /**
    * Bridge from optional json to optional value.
    *
    * @param self optional json value.
    */
  implicit class JsonBridge(val self: Option[Json]) extends AnyVal {

    /**
      * Returns current optional json value as json object.
      *
      * @return current optional json value as json object if possible, otherwise [[None]].
      */
    def asObject: Option[JsObject] = self.flatMap(_.asObject)

    /**
      * Returns current optional json value as json array.
      *
      * @return current optional json value as json array if possible, otherwise [[None]].
      */
    def asArray: Option[JsArray] = self.flatMap(_.asArray)

    /**
      * Returns current optional json value as json number.
      *
      * @return current optional json value as json number if possible, otherwise [[None]].
      */
    def asNumber: Option[JsNumber] = self.flatMap(_.asNumber)

    /**
      * Returns current optional json value as byte string.
      *
      * @return current optional json value as byte string if possible, otherwise [[None]].
      */
    def asBytes: Option[ByteString] = self.flatMap(_.asBytes)

    /**
      * Returns current optional json value as json boolean.
      *
      * @return current optional json value as json boolean if possible, otherwise [[None]].
      */
    def asBoolean: Option[Boolean] = self.flatMap(_.asBoolean)

    /**
      * Returns current optional json value as json string.
      *
      * @return current optional json value as json string if possible, otherwise [[None]].
      */
    def asString: Option[String] = self.flatMap(_.asString)

    /**
      * Returns current optional json value as json null.
      *
      * @return current optional json value as json null if possible, otherwise [[None]].
      */
    def asNull: Option[Unit] = self.flatMap(_.asNull)

    /**
      * Returns current optional json value as json int.
      *
      * @return current optional json value as json int if possible, otherwise [[None]].
      */
    def asInt: Option[Int] = self.flatMap(_.asInt)

    /**
      * Returns current optional json value as json long.
      *
      * @return current optional json value as json long if possible, otherwise [[None]].
      */
    def asLong: Option[Long] = self.flatMap(_.asLong)

    /**
      * Returns current optional json value as json big decimal.
      *
      * @return current optional json value as json big decimal if possible, otherwise [[None]].
      */
    def asBigDecimal: Option[BigDecimal] = self.flatMap(_.asBigDecimal)

    /**
      * Returns current optional json value as json double.
      *
      * @return current optional json value as json double if possible, otherwise [[None]].
      */
    def asDouble: Option[Double] = self.flatMap(_.asDouble)

    /**
      * Returns current optional json value as json float.
      *
      * @return current optional json value as json float if possible, otherwise [[None]].
      */
    def asFloat: Option[Float] = self.flatMap(_.asFloat)

  }

}
