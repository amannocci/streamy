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
  * Represent a json pointer.
  * Construction of json pointer can be slow because we compute only one time path.
  * Evaluation must be as fast as possible.
  *
  * @param underlying json pointer path.
  */
case class JsonPointer(private[json] val underlying: Array[JsonAccessor] = Array.empty) {

  /**
    * Apply json pointer to a json value.
    *
    * @param json json value to evaluate.
    * @return optional json value.
    */
  private[json] def apply(json: Json): Option[Json] = {
    if (underlying.isEmpty) {
      Some(json)
    } else {
      // Current computation
      var idx = 0
      var result: Option[Json] = Some(json)

      // Iterate over path accessor
      while (idx < underlying.length) {
        // Retrieve current accessor
        val accessor = underlying(idx)

        // Result of access
        val access = accessor.evaluate(result.get)
        if (access.isDefined) {
          idx += 1
          result = access
        } else {
          idx = underlying.length
          result = None
        }
      }

      // Result of computation
      result
    }
  }

  // scalastyle:off method.name
  /**
    * Create a new json pointer.
    *
    * @param key access key.
    * @return new json pointer.
    */
  def /(key: String): JsonPointer = {
    val newPath = new Array[JsonAccessor](underlying.length + 1)
    Array.copy(underlying, 0, newPath, 0, underlying.length)
    newPath.update(underlying.length, JsonObjectAccessor(key))
    copy(newPath)
  }
  // scalastyle:on method.name

  // scalastyle:off method.name
  /**
    * Create a new json pointer.
    *
    * @param idx access idx.
    * @return new json pointer.
    */
  def /(idx: Int): JsonPointer = {
    val newPath = new Array[JsonAccessor](underlying.length + 1)
    Array.copy(underlying, 0, newPath, 0, underlying.length)
    newPath.update(underlying.length, JsonArrayAccessor(idx))
    copy(newPath)
  }
  // scalastyle:on method.name

}

/**
  * Represent an abstract json accessor.
  */
private[json] trait JsonAccessor {

  def evaluate(json: Json): Option[Json]

  def set(json: Json, value: Json): Option[Json]

  def add(json: Json, value: Json): Option[Json]

  def replace(json: Json, value: Json): Option[Json]

  def remove(json: Json, mustExist: Boolean = true): Option[Json]

}

/**
  * Represent an object accessor.
  */
private[json] case class JsonObjectAccessor(key: String) extends JsonAccessor {

  def evaluate(json: Json): Option[Json] = json.asObject.flatMap(_ (key))

  @inline def set(json: Json, value: Json): Option[Json] = add(json, value)

  def add(json: Json, value: Json): Option[Json] = json.asObject.map { x =>
    x.underlying.put(key, value)
    x
  }

  def replace(json: Json, value: Json): Option[Json] = json.asObject.flatMap { x =>
    if (x.underlying.contains(key)) {
      x.underlying.put(key, value)
      Some(x)
    } else {
      None
    }
  }

  def remove(json: Json, mustExist: Boolean = true): Option[Json] = json.asObject.flatMap { x =>
    if (mustExist) {
      if (x.underlying.contains(key)) {
        x.underlying.remove(key)
        Some(x)
      } else {
        None
      }
    } else {
      x.underlying.remove(key)
      Some(x)
    }
  }

}

/**
  * Represent an array accessor.
  */
private[json] case class JsonArrayAccessor(idx: Int) extends JsonAccessor {

  def evaluate(json: Json): Option[Json] = json.asArray.flatMap(_ (idx))

  @inline def set(json: Json, value: Json): Option[Json] = replace(json, value)

  def add(json: Json, value: Json): Option[Json] = json.asArray.flatMap { x =>
    if (idx > -1 && idx < x.underlying.length) {
      x.underlying.insert(idx, value)
      Some(x)
    } else if (idx == -1) {
      x.underlying.append(value)
      Some(x)
    } else {
      None
    }
  }

  def replace(json: Json, value: Json): Option[Json] = json.asArray.flatMap { x =>
    if (idx > -1 && idx < x.underlying.length) {
      x.underlying.update(idx, value)
      Some(x)
    } else {
      None
    }
  }

  def remove(json: Json, mustExist: Boolean = true): Option[Json] = json.asArray.flatMap { x =>
    if (idx > -1 && idx < x.underlying.length) {
      x.underlying.remove(idx)
      Some(x)
    } else {
      if (mustExist) {
        None
      } else {
        Some(x)
      }
    }
  }

}
