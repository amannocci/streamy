/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2019
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

import scala.collection.mutable.ArrayBuffer

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
  def apply(json: Json): MaybeJson

}

/**
  * Abstract operation implementation.
  *
  * @param path json path.
  */
private[json] abstract class AbstractOperation(path: JsonPointer) extends JsonOperation {

  // Shortcut to underlying data structure
  val underlying: ArrayBuffer[JsModifier] = path.underlying

  /**
    * Apply operation recursively.
    *
    * @param path    json path to use.
    * @param idx     current position in json path.
    * @param current current value explored.
    * @return json value modified or [[JsUndefined]].
    */
  private[json] def apply(
    path: JsonPointer,
    idx: Int,
    current: MaybeJson
  ): MaybeJson = {
    // We fail to evaluate path if not mapped
    current.flatMap[Json] { ref =>
      val modifier = underlying(idx)

      // We are in final state
      if (idx == underlying.length - 1) {
        operate(modifier, ref.copy())
      } else {
        // Recursive call until final state
        apply(path, idx + 1, modifier.get(ref)).flatMap[Json] { result =>
          modifier.set(ref.copy(), result)
        }
      }
    }
  }

  /**
    * Apply operation on current json value.
    *
    * @param accessor json value accessor.
    * @param current  current json value.
    * @return json value modified or [[JsUndefined]].
    */
  def operate(accessor: JsModifier, current: Json): MaybeJson

}

/**
  * Set a json value at pointed location based on an arbitrary function.
  *
  * @param path json path.
  * @param f    arbitrary function.
  */
private[json] case class SetFunc[T](path: JsonPointer, f: T => Json)(implicit c: JsTyped[T]) extends AbstractOperation(path) {

  override def apply(json: Json): MaybeJson =
    if (path.isEmpty) {
      f(json.get[T])
    } else {
      apply(path, 0, json)
    }

  def operate(modifier: JsModifier, current: Json): MaybeJson =
    modifier.get(current).flatMap[T](v => modifier.set(current, f(v)))

}

/**
  * Add a json value at pointed location.
  *
  * @param path  json path.
  * @param value json value to add with.
  */
case class Add(path: JsonPointer, value: Json) extends AbstractOperation(path) {

  override def apply(json: Json): MaybeJson =
    if (path.isEmpty) {
      value
    } else {
      apply(path, 0, json)
    }

  def operate(modifier: JsModifier, current: Json): MaybeJson =
    modifier.add(current, value)

}

/**
  * Replace a json value at pointed location.
  *
  * @param path  json path.
  * @param value json value to replace with.
  */
case class Replace(path: JsonPointer, value: Json) extends AbstractOperation(path) {

  override def apply(json: Json): MaybeJson =
    if (path.isEmpty) {
      value
    } else {
      apply(path, 0, json)
    }

  def operate(modifier: JsModifier, current: Json): MaybeJson =
    modifier.replace(current, value)

}

/**
  * Remove a json value at pointed location.
  *
  * @param path      json path.
  * @param mustExist whether the json value must exist to successed.
  */
case class Remove(path: JsonPointer, mustExist: Boolean = true) extends AbstractOperation(path) {

  override def apply(json: Json): MaybeJson =
    if (path.isEmpty) {
      json
    } else {
      val result = apply(path, 0, json)
      if (mustExist) {
        result
      } else {
        result.orElse(json)
      }
    }

  def operate(modifier: JsModifier, current: Json): MaybeJson =
    modifier.remove(current, mustExist)

}

/**
  * Move a json value from pointed location to another pointed location.
  *
  * @param from from json path location.
  * @param to   to json path location.
  */
case class Move(from: JsonPointer, to: JsonPointer) extends JsonOperation {

  def apply(json: Json): MaybeJson =
    json.evaluate(from)
      .flatMap[Json](r => Remove(from)(json).flatMap[Json](Add(to, r)(_)))

}

/**
  * Copy a json value from pointed location to another pointed location.
  *
  * @param from from json path location.
  * @param to   to json path location.
  */
case class Copy(from: JsonPointer, to: JsonPointer) extends JsonOperation {

  def apply(json: Json): MaybeJson = json.evaluate(from).flatMap[Json](Add(to, _)(json))

}

/**
  * Test if a json value at pointed location is equal to another one.
  *
  * @param path  json path location.
  * @param value json value to compate with.
  */
case class Test(path: JsonPointer, value: Json) extends JsonOperation {

  def apply(json: Json): MaybeJson =
    json.evaluate(path).flatMap[Json] { x =>
      if (x.equals(value)) {
        json
      } else {
        JsUndefined
      }
    }

}
