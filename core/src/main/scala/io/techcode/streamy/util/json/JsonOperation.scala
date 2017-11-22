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
package io.techcode.streamy.util.json

/**
  * Json operations implementation compliant with RFC-6902.
  * https://tools.ietf.org/html/rfc6902
  */
sealed trait JsonOperation {

  /**
    * Apply operation on json value.
    *
    * @param json json value to operate.
    * @return json value operate or none.
    */
  def apply(json: Json): Option[Json]

}

/**
  * Simple operation implementation.
  *
  * @param path json path.
  */
private[json] abstract class SimpleOperation(path: JsonPointer) extends JsonOperation {

  def apply(json: Json): Option[Json] = {
    if (path.underlying.isEmpty) {
      Some(json)
    } else {
      apply(path, 0, Some(json))
    }
  }

  private[json] def apply(path: JsonPointer, idx: Int, current: Option[Json]): Option[Json] = {
    if (current.isEmpty) {
      // We fail to evaluate path
      current
    } else if (idx == path.underlying.length - 1) {
      // We are in final state
      operate(path.underlying(idx), current)
    } else {
      // Recursive call until final state
      val result = apply(path, idx + 1, path.underlying(idx).evaluate(current.get))
      if (result.isDefined) {
        path.underlying(idx).set(current.get, result.get)
      } else {
        result
      }
    }
  }

  def operate(accessor: JsonAccessor, current: Option[Json]): Option[Json]

}

case class Add(path: JsonPointer, value: Json) extends SimpleOperation(path) {

  def operate(accessor: JsonAccessor, current: Option[Json]): Option[Json] =
    accessor.add(current.get, value)

}

case class Replace(path: JsonPointer, value: Json) extends SimpleOperation(path) {

  def operate(accessor: JsonAccessor, current: Option[Json]): Option[Json] =
    accessor.replace(current.get, value)

}

case class Remove(path: JsonPointer, mustExist: Boolean = true) extends SimpleOperation(path) {

  def operate(accessor: JsonAccessor, current: Option[Json]): Option[Json] =
    accessor.remove(current.get, mustExist)

}

case class Move(from: JsonPointer, to: JsonPointer) extends JsonOperation {

  def apply(json: Json): Option[Json] = {
    val result = json.evaluate(from)
    if (result.isDefined) {
      Remove(from).apply(json).flatMap(Add(to, result.get).apply(_))
    } else {
      result
    }
  }

}

case class Copy(from: JsonPointer, to: JsonPointer) extends JsonOperation {

  def apply(json: Json): Option[Json] = {
    val result = json.evaluate(from)
    if (result.isDefined) {
      Add(to, result.get).apply(json)
    } else {
      result
    }
  }

}

case class Test(path: JsonPointer, value: Json) extends JsonOperation {

  def apply(json: Json): Option[Json] = path(json).flatMap { x =>
    if (x.equals(value)) {
      Some(json)
    } else {
      None
    }
  }

}
