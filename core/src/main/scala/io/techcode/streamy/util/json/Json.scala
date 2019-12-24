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

import akka.util.ByteString
import io.techcode.streamy.util.lang.Primitives
import io.techcode.streamy.util.math.{RyuDouble, RyuFloat}
import io.techcode.streamy.util.parser.{ByteStringParser, StringParser}
import io.techcode.streamy.util.printer.{ByteStringPrinter, PrintException, StringPrinter}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Json {

  // Thread safe bytestring printer
  private val byteStringPrinter = ThreadLocal.withInitial[ByteStringPrinter[Json]](() => JsonPrinter.byteStringPrinter())

  // Thread safe string printer
  private val stringPrinter = ThreadLocal.withInitial[StringPrinter[Json]](() => JsonPrinter.stringPrinter())

  // Thread safe bytestring parser
  private val byteStringParser = ThreadLocal.withInitial[ByteStringParser[Json]](() => JsonParser.byteStringParser())

  // Thread safe string parser
  private val stringParser = ThreadLocal.withInitial[StringParser[Json]](() => JsonParser.stringParser())

  // Singleton json object
  private val jsonObjEmpty = JsObject(mutable.HashMap())

  // Singleton json array
  private val jsonArrayEmpty = JsArray(mutable.ArrayBuffer())

  /**
    * Construct a new JsObject, with the order of fields in the Seq.
    */
  def obj(fields: (String, Json)*): JsObject =
    if (fields.isEmpty) {
      jsonObjEmpty
    } else {
      JsObject(mutable.HashMap(fields: _*))
    }

  /**
    * Construct a new JsArray with given values.
    */
  def arr(values: Json*): JsArray =
    if (values.isEmpty) {
      jsonArrayEmpty
    } else {
      JsArray(mutable.ArrayBuffer(values: _ *))
    }

  /**
    * Create a new json object builder.
    *
    * @return json object builder.
    */
  def objectBuilder(): JsObjectBuilder =
    new mutable.GrowableBuilder[(String, Json), JsObject](Json.obj()) {
      var underlying: mutable.HashMap[String, Json] = mutable.HashMap[String, Json]()

      override def clear(): Unit = underlying = mutable.HashMap[String, Json]()

      override def result(): JsObject = JsObject(underlying)

      override def addOne(elem: (String, Json)): this.type = {
        underlying += elem
        this
      }

      override def addAll(xs: IterableOnce[(String, Json)]): this.type = {
        underlying.addAll(xs)
        this
      }

      override def knownSize: Int = underlying.knownSize
    }

  /**
    * Create a new json array builder.
    *
    * @return json array builder.
    */
  def arrayBuilder(): JsArrayBuilder =
    new mutable.GrowableBuilder[Json, JsArray](Json.arr()) {
      var underlying: mutable.ArrayBuffer[Json] = mutable.ArrayBuffer[Json]()

      override def clear(): Unit = underlying = mutable.ArrayBuffer[Json]()

      override def result(): JsArray = JsArray(underlying)

      override def addOne(elem: Json): this.type = {
        underlying += elem
        this
      }

      override def addAll(xs: IterableOnce[Json]): this.type = {
        underlying.addAll(xs)
        this
      }

      override def knownSize: Int = underlying.knownSize
    }

  /**
    * Parses a bytestring representing a Json input, and returns it as a [[Json]].
    *
    * @param data the bytestring to parse.
    */
  @inline def parseByteString(data: ByteString): Either[Throwable, Json] = byteStringParser.get().parse(data)

  /**
    * Parses unsafely a bytestring representing a Json input, and returns it as a [[Json]].
    *
    * @param data the bytestring to parse.
    */
  @inline def parseByteStringUnsafe(data: ByteString): Json = eitherToValue(parseByteString(data))

  /**
    * Parses a bytes array representing a Json input, and returns it as a [[Json]].
    *
    * @param data the string to parse.
    */
  @inline def parseBytes(data: Array[Byte]): Either[Throwable, Json] = byteStringParser.get().parse(ByteString.fromArrayUnsafe(data))

  /**
    * Parses a bytes array representing a Json input, and returns it as a [[Json]].
    *
    * @param data the string to parse.
    */
  @inline def parseBytesUnsafe(data: Array[Byte]): Json = eitherToValue(parseBytes(data))

  /**
    * Parses a string representing a Json input, and returns it as a [[Json]].
    *
    * @param input the string to parse.
    */
  @inline def parseString(input: String): Either[Throwable, Json] = stringParser.get().parse(input)

  /**
    * Parses unsafely a string representing a Json input, and returns it as a [[Json]].
    *
    * @param input the string to parse.
    */
  @inline def parseStringUnsafe(input: String): Json = eitherToValue(parseString(input))

  /**
    * Prints a [[Json]] to its bytestring representation.
    *
    * @return a bytestring with the json representation.
    */
  @inline def printByteString(json: Json): Either[PrintException, ByteString] = byteStringPrinter.get().print(json)

  /**
    * Prints unsafely a [[Json]] to its bytestring representation.
    *
    * @return a bytestring with the json representation.
    */
  @inline def printByteStringUnsafe(json: Json): ByteString = eitherToValue(printByteString(json))

  /**
    * Prints a [[Json]] to its string representation.
    *
    * @return a string with the json representation.
    */
  @inline def printString(json: Json): Either[PrintException, String] = stringPrinter.get().print(json)

  /**
    * Prints unsafely a [[Json]] to its string representation.
    *
    * @return a string with the json representation.
    */
  @inline def printStringUnsafe(json: Json): String = eitherToValue(printString(json))

  /**
    * Extract value from either and throw exception if needed.
    *
    * @param e either.
    * @tparam Error error to throw in case.
    * @tparam Value real value.
    * @return value in best case.
    */
  private def eitherToValue[Error <: Throwable, Value](e: Either[Error, Value]) = e match {
    case Right(value) => value
    case Left(ex) => throw ex
  }

}

/**
  * Represents an optional json representation.
  */
trait MaybeJson {

  /**
    * Returns true if the json is JsUndefined, false otherwise.
    */
  def isEmpty: Boolean

  /**
    * Returns true if the option is an instance of JsDefined, false otherwise.
    */
  def isDefined: Boolean = !isEmpty

  /**
    * Returns the option's value.
    *
    * @note The option must be nonEmpty.
    * @throws NoSuchElementException if the option is empty.
    */
  def get[T](implicit c: JsTyped[T]): T

  /**
    * Returns this JsDefined if it is nonempty '''and''' applying the predicate $p to
    * this JsDefined value returns true. Otherwise, return JsUndefined.
    *
    * @param  p the predicate used for testing.
    */
  @inline def filter(p: Json => Boolean): MaybeJson

  /**
    * Returns this JsDefined if it is nonempty '''and''' applying the predicate $p to
    * this JsDefined value returns false. Otherwise, return JsUndefined.
    *
    * @param  p the predicate used for testing.
    */
  @inline def filterNot(p: Json => Boolean): MaybeJson

  /**
    * Apply the given procedure $f to the option's value,
    * if it is nonempty. Otherwise, do nothing.
    *
    * @param  f the procedure to apply.
    * @see map
    */
  @inline def ifExists[T](f: T => Unit)(implicit c: JsTyped[T]): Unit

  /**
    * Returns the result of applying $f to this $option's
    * value if the $option is nonempty.  Otherwise, evaluates
    * expression `ifEmpty`.
    *
    * @param  ifEmpty the expression to evaluate if empty.
    * @param  f       the function to apply if nonempty.
    */
  @inline def fold(ifEmpty: => Json)(f: Json => Json): Json

  /**
    * Returns the option's value if the option is nonempty, otherwise
    * return the result of evaluating `default`.
    *
    * @param default the default expression.
    */
  @inline def getOrElse[T](default: => T)(implicit c: JsTyped[T]): T

  /**
    * Returns a $some containing the result of applying $f to this $option's
    * value if this $option is nonempty.
    * Otherwise return $none.
    *
    * @note This is similar to `flatMap` except here,
    *       $f does not need to wrap its result in an $option.
    * @param  f the function to apply
    */
  @inline def map[T](f: T => Json)(implicit c: JsTyped[T]): MaybeJson

  /**
    * Returns the result of applying $f to this $option's value if
    * this $option is nonempty.
    * Returns $none if this $option is empty.
    * Slightly different from `map` in that $f is expected to
    * return an $option (which could be $none).
    *
    * @param  f the function to apply
    * @see map
    * @see foreach
    */
  @inline def flatMap[T](f: T => MaybeJson)(implicit c: JsTyped[T]): MaybeJson

  /**
    * Returns true if this option is nonempty '''and''' the predicate
    * $p returns true when applied to this $option's value.
    * Otherwise, returns false.
    *
    * @param  p the predicate to test
    */
  @inline def exists(p: Json => Boolean): Boolean

  /** Returns this $option if it is nonempty,
    * otherwise return the result of evaluating `alternative`.
    *
    * @param alternative the alternative expression.
    */
  @inline def orElse(alternative: => MaybeJson): MaybeJson

  /**
    * Returns true if the current json value is a json object.
    *
    * @return true if the current json value is a json object, otherwise false.
    */
  def isObject: Boolean = false

  /**
    * Returns true if the current json value is a json array.
    *
    * @return true if the current json value is a json array, otherwise false.
    */
  def isArray: Boolean = false

  /**
    * Returns true if the current json value is a json number.
    *
    * @return true if the current json value is a json number, otherwise false.
    */
  def isNumber: Boolean = false

  /**
    * Returns true if the current json value is a byte string.
    *
    * @return true if the current json value is a byte string, otherwise false.
    */
  def isBytes: Boolean = false

  /**
    * Returns true if the current json value is a boolean.
    *
    * @return true if the current json value is a boolean, otherwise false.
    */
  def isBoolean: Boolean = false

  /**
    * Returns true if the current json value is a string.
    *
    * @return true if the current json value is a string, otherwise false.
    */
  def isString: Boolean = false

  /**
    * Returns true if the current json value is a big decimal.
    *
    * @return true if the current json value is a big decimal, otherwise false.
    */
  def isBigDecimal: Boolean = false

  /**
    * Returns true if the current json value is a null.
    *
    * @return true if the current json value is a null, otherwise false.
    */
  def isNull: Boolean = false

  /**
    * Returns true if the current json value is an int.
    *
    * @return true if the current json value is an int, otherwise false.
    */
  def isInt: Boolean = false

  /**
    * Returns true if the current json value is an long.
    *
    * @return true if the current json value is an long, otherwise false.
    */
  def isLong: Boolean = false

  /**
    * Returns true if the current json value is a double.
    *
    * @return true if the current json value is a double, otherwise false.
    */
  def isDouble: Boolean = false

  /**
    * Returns true if the current json value is a float.
    *
    * @return true if the current json value is a float, otherwise false.
    */
  def isFloat: Boolean = false

}

/**
  * Represents a defined json.
  */
abstract class JsDefined extends MaybeJson {

  def isEmpty: Boolean = false

  def filter(p: Json => Boolean): MaybeJson = if (p(this.get[Json])) this else JsUndefined

  def filterNot(p: Json => Boolean): MaybeJson = if (!p(this.get[Json])) this else JsUndefined

  def ifExists[T](f: T => Unit)(implicit c: JsTyped[T]): Unit = c.ifExists(this, f)

  def fold(ifEmpty: => Json)(f: Json => Json): Json = f(this.get[Json])

  def getOrElse[T](default: => T)(implicit c: JsTyped[T]): T = c.getOrElse(this, default)

  def map[T](f: T => Json)(implicit c: JsTyped[T]): MaybeJson = c.map(this, f)

  def flatMap[T](f: T => MaybeJson)(implicit c: JsTyped[T]): MaybeJson = c.flatMap(this, f)

  def exists(p: Json => Boolean): Boolean = p(this.get[Json])

  def orElse(alternative: => MaybeJson): MaybeJson = this

}

/**
  * Represents an defined json.
  */
object JsUndefined extends MaybeJson {

  override def isEmpty: Boolean = true

  override def get[T](implicit c: JsTyped[T]): T = throw new NoSuchElementException("JsUndefined.get")

  def filter(p: Json => Boolean): MaybeJson = JsUndefined

  def filterNot(p: Json => Boolean): MaybeJson = JsUndefined

  def ifExists[T](f: T => Unit)(implicit c: JsTyped[T]): Unit = ()

  def fold(ifEmpty: => Json)(f: Json => Json): Json = ifEmpty

  def getOrElse[T](default: => T)(implicit c: JsTyped[T]): T = default

  def map[T](f: T => Json)(implicit c: JsTyped[T]): MaybeJson = JsUndefined

  def flatMap[T](f: T => MaybeJson)(implicit c: JsTyped[T]): MaybeJson = JsUndefined

  def exists(p: Json => Boolean): Boolean = false

  def orElse(alternative: => MaybeJson): MaybeJson = alternative

}

// scalastyle:off
/**
  * Generic json value.
  */
sealed trait Json extends JsDefined {

  def get[T](implicit c: JsTyped[T]): T = c.get(this)

  /**
    * Evaluate the given path in the given json value.
    *
    * @param path path to used.
    * @return value if founded, otherwise [[None]].
    */
  def evaluate(path: JsonPointer): MaybeJson = path(this)

  /**
    * Patch given json value by applying all json operations in a transactionnal way.
    *
    * @param op  operation to apply first.
    * @param ops seq of operations to apply.
    * @return json value patched or original.
    */
  def patch(op: JsonOperation, ops: JsonOperation*): MaybeJson = {
    var result: MaybeJson = op(this)
    ops.takeWhile(_ => result.isDefined).foreach(op => result = op(result.get[Json]))
    result
  }

  /**
    * Patch given json value by applying all json operations in a transactionnal way.
    *
    * @param ops seq of operations to apply.
    * @return json value patched or original.
    */
  def patch(ops: Seq[JsonOperation]): MaybeJson = {
    var result: MaybeJson = this
    ops.takeWhile(_ => result.isDefined).foreach(op => result = op(result.get[Json]))
    result
  }

  /**
    * Returns a copy of current json value.
    *
    * @return current json value.
    */
  def copy(): Json

  /**
    * Size hint of the string representation of this element in Json.
    *
    * @return size of the element.
    */
  def sizeHint(): Int = toString.length

  override def toString: String = Json.printStringUnsafe(this)

}

// scalastyle:on

/**
  * Represent a json null value.
  */
case object JsNull extends Json {

  override val isNull: Boolean = true

  override val copy: Json = this

  override val sizeHint: Int = 4

}

/**
  * Represent a json boolean value.
  *
  * @param value underlying value.
  */
sealed abstract class JsBoolean(value: Boolean) extends Json {

  override val isBoolean: Boolean = true

  override val copy: Json = this

}

/**
  * Represents Json Boolean True value.
  */
case object JsTrue extends JsBoolean(true) {

  override val sizeHint: Int = 4

}

/**
  * Represents Json Boolean False value.
  */
case object JsFalse extends JsBoolean(false) {

  override val sizeHint: Int = 5

}

/**
  * Represents a Json number value.
  */
trait JsNumber extends Json {

  override val isNumber: Boolean = true

  /**
    * Returns the value of json number as an Int.
    */
  @inline def toInt: Int

  /**
    * Returns the value of json number as an Long.
    */
  @inline def toLong: Long

  /**
    * Returns the value of json number as an Float.
    */
  @inline def toFloat: Float

  /**
    * Returns the value of json number as an Double.
    */
  @inline def toDouble: Double

  /**
    * Returns the value of json number as an BigDecimal.
    */
  @inline def toBigDecimal: BigDecimal

}

/**
  * Represent a json int value.
  *
  * @param value underlying value.
  */
case class JsInt(value: Int) extends JsNumber {

  override val isInt: Boolean = true

  override def toInt: Int = value

  override def toLong: Long = value.toLong

  override def toFloat: Float = value.toFloat

  override def toDouble: Double = value.toDouble

  override def toBigDecimal: BigDecimal = BigDecimal(value)

  override def copy(): Json = this

  override lazy val sizeHint: Int = Primitives.stringSize(value)

}

/**
  * Represent a json long value.
  *
  * @param value underlying value.
  */
case class JsLong(value: Long) extends JsNumber {

  override val isLong: Boolean = true

  override def toInt: Int = value.toInt

  override def toLong: Long = value

  override def toFloat: Float = value.toFloat

  override def toDouble: Double = value.toDouble

  override def toBigDecimal: BigDecimal = BigDecimal(value)

  override def copy(): Json = this

  // Dividing with powers of two
  override lazy val sizeHint: Int = Primitives.stringSize(value)

}

/**
  * Represent a json float value.
  *
  * @param value underlying value.
  */
case class JsFloat(value: Float) extends JsNumber {

  override val isFloat: Boolean = true

  override def toInt: Int = value.toInt

  override def toLong: Long = value.toLong

  override def toFloat: Float = value

  override def toDouble: Double = value.toDouble

  override def toBigDecimal: BigDecimal = BigDecimal(value)

  override def copy(): Json = this

  override lazy val sizeHint: Int = RyuFloat.toString(value).length

}

/**
  * Represent a json double value.
  *
  * @param value underlying value.
  */
case class JsDouble(value: Double) extends JsNumber {

  override val isDouble: Boolean = true

  override def toInt: Int = value.toInt

  override def toLong: Long = value.toLong

  override def toFloat: Float = value.toFloat

  override def toDouble: Double = value

  override def toBigDecimal: BigDecimal = BigDecimal(value)

  override def copy(): Json = this

  override lazy val sizeHint: Int = RyuDouble.toString(value).length

}

/**
  * Represent a json big decimal value.
  *
  * @param value underlying value.
  */
case class JsBigDecimal(value: BigDecimal) extends JsNumber {

  override val isBigDecimal: Boolean = true

  override def toInt: Int = value.toInt

  override def toLong: Long = value.toLong

  override def toFloat: Float = value.toFloat

  override def toDouble: Double = value.toDouble

  override def toBigDecimal: BigDecimal = value

  override def copy(): Json = this

}

/**
  * Represent a json string value.
  *
  * @param value underlying value.
  */
case class JsString(value: String) extends Json {

  override val isString: Boolean = true

  override def copy(): Json = this

  override val sizeHint: Int = value.length + 2

}

/**
  * Represent a json bytes value.
  *
  * @param value underlying value.
  */
case class JsBytes(value: ByteString) extends Json {

  override val isBytes: Boolean = true

  override def copy(): Json = this

}

/**
  * Represent a json array value.
  *
  * @param underlying underlying structure.
  */
case class JsArray private[json](
  private[json] val underlying: ArrayBuffer[Json]
) extends Json with mutable.Growable[Json] {

  /**
    * Get element at a given index.
    *
    * @param idx index to use.
    * @return element at given index.
    */
  def apply(idx: Int): MaybeJson = {
    if (idx >= 0 && idx < underlying.length) {
      underlying(idx)
    } else {
      JsUndefined
    }
  }

  /**
    * Retrieve the head element of this json array.
    *
    * @return head element of this json array.
    */
  def head(): MaybeJson = {
    if (underlying.nonEmpty) {
      underlying(0)
    } else {
      JsUndefined
    }
  }

  /**
    * Retrieve the last element of this json array.
    *
    * @return last element of this json array.
    */
  def last(): MaybeJson = {
    if (underlying.nonEmpty) {
      underlying(underlying.length - 1)
    } else {
      JsUndefined
    }
  }

  /**
    * Append an other array with the elements of this array.
    *
    * @param other array to append with.
    * @return new json array.
    */
  def append(other: JsArray): JsArray = {
    val builder = new ArrayBuffer[Json](underlying.length + other.underlying.length)
    builder ++= underlying
    builder ++= other.underlying
    JsArray(builder)
  }

  /**
    * Append an element to this array.
    *
    * @param el element to append.
    * @return new json array.
    */
  def append(el: Json): JsArray = {
    val builder = new ArrayBuffer[Json](underlying.length + 1)
    builder ++= underlying
    builder += el
    JsArray(builder)
  }

  /**
    * Prepend this array with the elements of an other array.
    *
    * @param other array to prepend with.
    * @return new json array.
    */
  def prepend(other: JsArray): JsArray = {
    val builder = new ArrayBuffer[Json](other.underlying.length + underlying.length)
    builder ++= other.underlying
    builder ++= underlying
    JsArray(builder)
  }

  /**
    * Prepend an element to this array.
    *
    * @param el element to prepend.
    * @return new json array.
    */
  def prepend(el: Json): JsArray = {
    val builder = new ArrayBuffer[Json](underlying.length + 1)
    builder += el
    builder ++= underlying
    JsArray(builder)
  }

  /**
    * Returns an Iterator over the elements in this JsArray.
    *
    * @return an Iterator containing all elements of this JsArray.
    */
  def iterator: Iterator[Json] = underlying.view.iterator

  /**
    * Returns a Seq containing all elements in this JsArray.
    *
    * @return a Seq containing all elements of this JsArray.
    */
  def toSeq: Seq[Json] = underlying.toSeq

  override val isArray: Boolean = true

  def copy(): Json = JsArray(underlying.clone())

  override lazy val sizeHint: Int = underlying.map(_.sizeHint()).sum + 1 + underlying.size

  // Support Growable interface for JsArray builder
  def addOne(elem: Json): JsArray.this.type = throw new NotImplementedError("`addOne` on JsArray isn't supported")

  // Support Growable interface for JsArray builder
  def clear(): Unit = throw new NotImplementedError("`clear` on JsArray isn't supported")

}

/**
  * Represent a json object value.
  *
  * @param underlying underlying structure.
  */
case class JsObject private[json](
  private[json] val underlying: mutable.HashMap[String, Json]
) extends Json with mutable.Growable[(String, Json)] {

  /**
    * Applies a function f to all elements of this json object.
    *
    * @param f the function that is applied.
    */
  def foreach(f: ((String, Json)) => Unit): Unit = underlying.foreach[Unit](f)

  /**
    * Return all fields as a seq.
    *
    * @return seq that contains all fields.
    */
  def fields: Seq[(String, Json)] = underlying.toSeq

  /**
    * Return all fields as a set.
    *
    * @return set that contains all fields.
    */
  def fieldSet: Set[(String, Json)] = underlying.toSet

  /**
    * Return all values.
    *
    * @return all values as iterable.
    */
  def values: Iterable[Json] = underlying.values

  /**
    * Returns the value to which the specified key is mapped, or JsUndefined.
    *
    * @param key key whose associated value is to be returned.
    * @return the value to which the specified key is mapped, or JsUndefined.
    */
  def apply(key: String): MaybeJson = underlying.getOrElse(key, JsUndefined)

  /**
    * Merge this object with another one.
    *
    * @return new json object merged.
    */
  def merge(other: JsObject): JsObject = JsObject(underlying ++ other.underlying)

  /**
    * Deep merge this object with another one.
    *
    * @param other other json value.
    * @return deep merged json value or none if failed.
    */
  def deepMerge(other: JsObject): JsObject = {
    def merge(left: JsObject, right: JsObject): JsObject = {
      val result = left.underlying.clone()

      right.underlying.foreach {
        case (rightKey, rightValue) =>
          val maybeExistingValue = left.underlying.get(rightKey)

          val newValue = (maybeExistingValue, rightValue) match {
            case (Some(e: JsObject), o: JsObject) => merge(e, o)
            case _ => rightValue
          }

          result += rightKey -> newValue
      }

      JsObject(result)
    }

    merge(this, other)
  }

  /**
    * Removes the specified key from this map if present.
    *
    * @param key key whose mapping is to be removed from the json object.
    * @return new json object.
    */
  def remove(key: String): JsObject =
    if (underlying.contains(key)) {
      JsObject(underlying.clone() -= key)
    } else {
      this
    }

  /**
    * Put the specified value with the specified key in this JsObject.
    *
    * @param field key and value to be associated.
    * @return new json object.
    */
  def put(field: (String, Json)): JsObject = JsObject(underlying.clone() += field)

  /**
    * Convert a json value to dot notation.
    *
    * @return json value with dot notation.
    */
  def flatten(): JsObject = flatten(this)

  /**
    * Convert a json object to dot notation.
    *
    * @param js     json object to flatten.
    * @param prefix accumultator.
    * @return json object with dot notation.
    */
  private def flatten(js: JsObject, prefix: String = ""): JsObject = js.fields.foldLeft[JsObject](Json.obj()) {
    case (acc, (k, v: Json)) =>
      v match {
        case obj: JsObject =>
          // Deep merge will always successed
          if (prefix.isEmpty) {
            acc.deepMerge(flatten(obj, k))
          } else {
            acc.deepMerge(flatten(obj, s"$prefix.$k"))
          }
        case _ =>
          if (prefix.isEmpty) {
            acc.put(k, v)
          } else {
            acc.put(s"$prefix.$k", v)
          }
      }
  }

  override val isObject: Boolean = true

  def copy(): Json = JsObject(underlying.clone())

  override lazy val sizeHint: Int =
    1 + underlying.keys.map(_.length + 2).sum + underlying.values.map(_.sizeHint()).sum + (underlying.values.size * 2)

  // Support Growable interface for JsObject builder
  def addOne(elem: (String, Json)): JsObject.this.type = throw new NotImplementedError("`addOne` on JsObject isn't supported")

  // Support Growable interface for JsObject builder
  def clear(): Unit = throw new NotImplementedError("`clear` on JsObject isn't supported")

}
