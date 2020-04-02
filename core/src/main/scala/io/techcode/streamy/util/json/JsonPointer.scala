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

import com.google.common.base.CharMatcher
import io.techcode.streamy.util.parser.{CharMatchers, ParseException, StringParser}

/**
  * Represent a json pointer.
  * Construction of json pointer can be slow because we compute only one time path.
  * Evaluation must be as fast as possible.
  *
  * @param underlying json pointer path.
  */
case class JsonPointer(private[json] val underlying: Array[JsAccessor] = Array.empty) extends Iterable[Either[String, Int]] {

  /**
    * Returns true if the json pointer is the root json pointer.
    */
  def isRoot: Boolean = underlying.isEmpty

  /**
    * Apply json pointer to a json value.
    *
    * @param json json value to evaluate.
    * @return optional json value.
    */
  private[json] def apply(json: Json): MaybeJson = {
    if (underlying.isEmpty) {
      json
    } else {
      // Current computation
      var idx = 0
      var result: MaybeJson = json

      // Iterate over path accessor
      while (idx < underlying.length) {
        // Retrieve current accessor
        val accessor = underlying(idx)

        // Result of access
        val access = accessor.evaluate(result.get[Json])
        if (access.isDefined) {
          idx += 1
          result = access
        } else {
          idx = underlying.length
          result = JsUndefined
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
    val newPath = new Array[JsAccessor](underlying.length + 1)
    Array.copy(underlying, 0, newPath, 0, underlying.length)
    if (JsAccessor.LastRef.equals(key)) {
      newPath.update(underlying.length, JsObjectOrArrayAccessor)
    } else {
      newPath.update(underlying.length, JsObjectAccessor(key))
    }
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
    val newPath = new Array[JsAccessor](underlying.length + 1)
    Array.copy(underlying, 0, newPath, 0, underlying.length)
    newPath.update(underlying.length, JsArrayAccessor(idx))
    copy(newPath)
  }

  // scalastyle:on method.name

  override def toString: String = "/" + underlying.map(_.repr).mkString("/")

  override def equals(o: Any): Boolean = o match {
    case obj: JsonPointer => underlying.sameElements(obj.underlying)
    case _ => false
  }

  override def iterator: Iterator[Either[String, Int]] = new Iterator[Either[String, Int]] {
    var index: Int = 0

    override def hasNext: Boolean = index < underlying.length

    override def next(): Either[String, Int] = {
      val el = underlying(index)
      index += 1
      el match {
        case JsObjectAccessor(key) => Left(key)
        case JsArrayAccessor(idx) => Right(idx)
      }
    }

  }

}

/**
  * Represent an abstract json accessor.
  */
private[json] trait JsAccessor {

  def evaluate(json: Json): MaybeJson

  def set(json: Json, value: Json): MaybeJson

  def add(json: Json, value: Json): MaybeJson

  def replace(json: Json, value: Json): MaybeJson

  def remove(json: Json, mustExist: Boolean = true): MaybeJson

  def repr: String

}

/**
  * JsAccesor companion.
  */
private[json] object JsAccessor {

  val LastRef: String = "-"

}

/**
  * Represent an object or array accessor.
  */
private[json] case object JsObjectOrArrayAccessor extends JsObjectLikeAccessor {

  override val key: String = JsAccessor.LastRef

  override def evaluate(json: Json): MaybeJson = super.evaluate(json)
    .orElse(json.flatMap[JsArray](_.last()))

  override def add(json: Json, value: Json): MaybeJson = super.add(json, value)
    .orElse(json.flatMap[JsArray] { x =>
      x.underlying.append(value)
      x
    })

  override def replace(json: Json, value: Json): MaybeJson = super.replace(json, value)
    .orElse(json.flatMap[JsArray] { x =>
      x.underlying.update(x.underlying.length - 1, value)
      x
    })

  override def remove(json: Json, mustExist: Boolean = true): MaybeJson = super.remove(json, mustExist)
    .orElse(json.flatMap[JsArray] { x =>
      if (x.underlying.isEmpty) {
        if (mustExist) {
          JsUndefined
        } else {
          x
        }
      } else {
        x.underlying.remove(x.underlying.length - 1)
        x
      }
    })

  override def repr: String = JsAccessor.LastRef

}

/**
  * Trait mixin for common json object accessor logic.
  */
private[json] trait JsObjectLikeAccessor extends JsAccessor {

  def key: String

  def evaluate(json: Json): MaybeJson = json.flatMap[JsObject](_ (key))

  @inline def set(json: Json, value: Json): MaybeJson = add(json, value)

  def add(json: Json, value: Json): MaybeJson = json.flatMap[JsObject] { x =>
    x.underlying.update(key, value)
    x
  }

  def replace(json: Json, value: Json): MaybeJson = json.flatMap[JsObject] { x =>
    if (x.underlying.contains(key)) {
      x.underlying.update(key, value)
      x
    } else {
      JsUndefined
    }
  }

  def remove(json: Json, mustExist: Boolean = true): MaybeJson = json.flatMap[JsObject] { x =>
    if (mustExist) {
      if (x.underlying.contains(key)) {
        x.underlying.remove(key)
        x
      } else {
        JsUndefined
      }
    } else {
      x.underlying.remove(key)
      x
    }
  }

  def repr: String = key

}

/**
  * Represent an object accessor.
  */
private[json] case class JsObjectAccessor(key: String) extends JsObjectLikeAccessor

/**
  * Represent an array accessor.
  */
private[json] case class JsArrayAccessor(idx: Int) extends JsAccessor {

  def evaluate(json: Json): MaybeJson = json.flatMap[JsArray](_ (idx))

  @inline def set(json: Json, value: Json): MaybeJson = replace(json, value)

  def add(json: Json, value: Json): MaybeJson = json.flatMap[JsArray] { x =>
    if (idx > -1 && idx < x.underlying.length) {
      x.underlying.insert(idx, value)
      x
    } else {
      JsUndefined
    }
  }

  def replace(json: Json, value: Json): MaybeJson = json.flatMap[JsArray] { x =>
    if (idx > -1 && idx < x.underlying.length) {
      x.underlying.update(idx, value)
      x
    } else {
      JsUndefined
    }
  }

  def remove(json: Json, mustExist: Boolean = true): MaybeJson = json.flatMap[JsArray] { x =>
    if (idx > -1 && idx < x.underlying.length) {
      x.underlying.remove(idx)
      x
    } else {
      if (mustExist) {
        JsUndefined
      } else {
        x
      }
    }
  }

  lazy val repr: String = idx.toString

}

/**
  * Json pointer parser companion.
  */
object JsonPointerParser {

  val Unescaped: CharMatcher = CharMatcher.inRange(0x00.toChar, 0x2E.toChar)
    .or(CharMatcher.inRange(0x30.toChar, 0x7D.toChar))
    .or(CharMatcher.inRange(0x7F.toChar, 0x10FFFF.toChar))
    .precomputed()

  // Json pointer parser
  private val parser = new JsonPointerParser()

  /**
    * Parses a string representing a Json pointer input, and returns it as a [[JsonPointer]].
    *
    * @param input the string to parse.
    */
  def parse(input: String): Either[Throwable, JsonPointer] = parser.parse(input)

  /**
    * Parses unsafely a string representing a Json pointer input, and returns it as a [[JsonPointer]].
    *
    * @param input the string to parse.
    */
  def parseUnsafe(input: String): JsonPointer = parse(input) match {
    case Right(value) => value
    case Left(ex) => throw ex
  }

}

/**
  * Json pointer parser.
  */
class JsonPointerParser extends StringParser[JsonPointer] {

  // Pointer builder
  private var pointer = Root

  /**
    * Process parsing based on [[data]] and current context.
    *
    * @return parsing result.
    */
  override def run(): JsonPointer = {
    if (root()) {
      pointer
    } else {
      throw new ParseException(s"Unexpected input at index ${cursor()}")
    }
  }

  override def root(): Boolean = zeroOrMore(
    ch('/') &&
      capture(refToken()) { rawToken =>
        if (rawToken.isEmpty) {
          false
        } else {
          val token = rawToken.replaceAll("~0", "~").replaceAll("~1", "/")
          if (CharMatchers.Digit.matchesAllOf(token)) {
            pointer = pointer / token.toInt
          } else {
            pointer = pointer / token
          }
          true
        }
      }
  ) && eoi()

  private def refToken(): Boolean = zeroOrMore(or(
    times(1, JsonPointerParser.Unescaped),
    escaped()
  ))

  private def escaped(): Boolean = ch('~') && or(ch('0'), ch('1'))

  override def cleanup(): Unit = {
    super.cleanup()
    pointer = Root
  }

}
