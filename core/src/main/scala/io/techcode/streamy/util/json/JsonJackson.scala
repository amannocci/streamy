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
package io.techcode.streamy.util.json

import java.io.{InputStream, StringWriter}
import java.math.{BigInteger, BigDecimal => JBigDec}

import akka.util.ByteString
import com.fasterxml.jackson.core._
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.{BigIntegerNode, DecimalNode}
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream

import scala.annotation.{switch, tailrec}
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.control.NonFatal

private[json] object JsonJackson {

  // Json mapper
  private val mapper = (new ObjectMapper).registerModule(StreamyModule)

  // Json factory
  private val factory = new JsonFactory(mapper)

  /**
    * Parses a bytestring representing a Json input, and returns it as a [[Json]].
    *
    * @param data the bytestring to parse.
    */
  def parse(data: ByteString): Either[Throwable, Json] = try {
    Right(mapper.readValue(factory.createParser(new ByteBufferBackedInputStream(data.asByteBuffer)), classOf[Json]))
  } catch {
    case NonFatal(error) => Left(error)
  }

  /**
    * Parses a string representing a Json input, and returns it as a [[Json]].
    *
    * @param data the string to parse.
    */
  def parse(data: Array[Byte]): Either[Throwable, Json] = try {
    Right(mapper.readValue(factory.createParser(data), classOf[Json]))
  } catch {
    case NonFatal(error) => Left(error)
  }

  /**
    * Parses a string representing a Json input, and returns it as a [[Json]].
    *
    * @param input the string to parse.
    */
  def parse(input: String): Either[Throwable, Json] = try {
    Right(mapper.readValue(factory.createParser(input), classOf[Json]))
  } catch {
    case NonFatal(error) => Left(error)
  }

  /**
    * Parses a stream representing a Json input, and returns it as a [[Json]].
    *
    * @param stream the InputStream to parse.
    */
  def parse(stream: InputStream): Either[Throwable, Json] = try {
    Right(mapper.readValue(factory.createParser(stream), classOf[Json]))
  } catch {
    case NonFatal(error) => Left(error)
  }

  /**
    * Converts a [[Json]] to its string representation.
    *
    * @return a String with the json representation.
    */
  def stringify(json: Json, escapeNonASCII: Boolean): String = withStringWriter { ctx =>
    val gen = stringJsonGenerator(ctx)

    if (escapeNonASCII) {
      gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII)
    }

    mapper.writeValue(gen, json)
    ctx.flush()
    ctx.getBuffer.toString
  }

  // Create a new string json generator
  private def stringJsonGenerator(out: java.io.StringWriter) = factory.createGenerator(out)

  // String writer context
  private def withStringWriter[T](f: StringWriter => T): T = {
    val sw = new StringWriter()
    try {
      f(sw)
    } catch {
      case err: Throwable => throw err
    } finally {
      if (sw != null) try {
        sw.close()
      } catch {
        case _: Throwable => ()
      }
    }
  }

}

/**
  * Register streamy json stuff in jackson.
  */
private[json] object StreamyModule extends SimpleModule("StreamyJson", Version.unknownVersion()) {

  override def setupModule(context: SetupContext) {
    context.addDeserializers(new StreamyDeserializers)
    context.addSerializers(new StreamySerializers)
  }

}

/**
  * Streamy deserializers implementations.
  */
private[json] class StreamyDeserializers extends Deserializers.Base {

  override def findBeanDeserializer(javaType: JavaType, config: DeserializationConfig, bean: BeanDescription): JsonValueDeserializer = {
    val classType = javaType.getRawClass
    if (classOf[Json].isAssignableFrom(classType)) {
      new JsonValueDeserializer(config.getTypeFactory, classType)
    } else {
      null
    }
  }

}

/**
  * Streamy serializers implementations.
  */
private[json] class StreamySerializers extends Serializers.Base {

  override def findSerializer(config: SerializationConfig, javaType: JavaType, bean: BeanDescription): JsonSerializer[Object] = {
    val ser: Object = if (classOf[Json].isAssignableFrom(bean.getBeanClass)) {
      JsonValueSerializer
    } else {
      null
    }
    ser.asInstanceOf[JsonSerializer[Object]]
  }

}

/**
  * Json serializer implementation.
  */
private[json] object JsonValueSerializer extends JsonSerializer[Json] {

  // Maximum magnitude of BigDecimal to write out as a plain string
  val MaxPlain: BigDecimal = 1e20

  // Minimum magnitude of BigDecimal to write out as a plain string
  val MinPlain: BigDecimal = 1e-10

  def serialize(value: Json, json: JsonGenerator, provider: SerializerProvider): Unit = {
    if (value.isObject) {
      json.writeStartObject()
      value.asObject.get.underlying.foreach { t =>
        json.writeFieldName(t._1)
        serialize(t._2, json, provider)
      }
      json.writeEndObject()
    } else if (value.isArray) {
      json.writeStartArray()
      value.asArray.get.underlying.foreach { v =>
        serialize(v, json, provider)
      }
      json.writeEndArray()
    } else if (value.isString) {
      json.writeString(value.asString.get)
    } else if (value.isBoolean) {
      json.writeBoolean(value.asBoolean.get)
    } else if (value.isInt) {
      json.writeNumber(value.asInt.get)
    } else if (value.isLong) {
      json.writeNumber(value.asLong.get)
    } else if (value.isFloat) {
      json.writeNumber(value.asFloat.get)
    } else if (value.isDouble) {
      json.writeNumber(value.asDouble.get)
    } else if (value.isBytes) {
      json.writeBinary(value.asBytes.get.toArray[Byte])
    } else if (value.isNumber) {
      // Workaround #3784: Same behaviour as if JsonGenerator were
      // configured with WRITE_BIGDECIMAL_AS_PLAIN, but forced as this
      // configuration is ignored when called from ObjectMapper.valueToTree
      val shouldWritePlain = {
        val va = value.asNumber.get.abs
        va < MaxPlain && va > MinPlain
      }
      val stripped = value.asNumber.get.bigDecimal.stripTrailingZeros
      val raw = if (shouldWritePlain) stripped.toPlainString else stripped.toString

      if (raw.indexOf('E') < 0 && raw.indexOf('.') < 0) {
        json.writeTree(new BigIntegerNode(new BigInteger(raw)))
      } else {
        json.writeTree(new DecimalNode(new JBigDec(raw)))
      }
    } else {
      json.writeNull()
    }
  }

}

/**
  * Json deserializer implementation.
  */
private[json] class JsonValueDeserializer(factory: TypeFactory, classType: Class[_]) extends JsonDeserializer[Object] {

  override final def isCachable: Boolean = true

  override def deserialize(parser: JsonParser, ctx: DeserializationContext): Json = {
    val value: Json = deserialize(parser, ctx, mutable.ListBuffer())
    if (!classType.isAssignableFrom(value.getClass)) {
      ctx.handleUnexpectedToken(classType, parser)
    }
    value
  }

  @tailrec final def deserialize(parser: JsonParser, ctx: DeserializationContext, parserContext: ListBuffer[DeserializerContext]): Json = {
    if (parser.currentToken() == null) parser.nextToken()

    val (maybeValue, nextContext) = (parser.getCurrentToken.id(): @switch) match {
      case JsonTokenId.ID_NUMBER_INT =>
        val inter = parser.getBigIntegerValue
        if (inter.bitLength() >= 32) {
          (Some(JsLong(inter.longValueExact())), parserContext)
        } else {
          (Some(JsInt(inter.intValueExact())), parserContext)
        }
      case JsonTokenId.ID_NUMBER_FLOAT => (Some(JsBigDecimal(parser.getDecimalValue)), parserContext)
      case JsonTokenId.ID_STRING => (Some(JsString(parser.getText)), parserContext)
      case JsonTokenId.ID_TRUE => (Some(JsTrue), parserContext)
      case JsonTokenId.ID_FALSE => (Some(JsFalse), parserContext)
      case JsonTokenId.ID_NULL => (Some(JsNull), parserContext)
      case JsonTokenId.ID_START_ARRAY => (None, ReadingList(new ArrayBuffer[Json]) +: parserContext)

      case JsonTokenId.ID_END_ARRAY => parserContext match {
        case ReadingList(content) +: stack => (Some(JsArray(content)), stack)
        case _ => throw new RuntimeException("We weren't reading a list, something went wrong")
      }

      case JsonTokenId.ID_START_OBJECT => (None, ReadingMap(mutable.AnyRefMap[String, Json]()) +: parserContext)

      case JsonTokenId.ID_FIELD_NAME => parserContext match {
        case (ctx: ReadingMap) +: stack => (None, ctx.setField(parser.getCurrentName) +: stack)
        case _ => throw new RuntimeException("We weren't reading an object, something went wrong")
      }

      case JsonTokenId.ID_END_OBJECT => parserContext match {
        case ReadingMap(content) +: stack => (Some(JsObject(content)), stack)
        case _ => throw new RuntimeException("We weren't reading an object, something went wrong")
      }

      case JsonTokenId.ID_NOT_AVAILABLE =>
        throw new RuntimeException("We weren't reading an object, something went wrong")

      case JsonTokenId.ID_EMBEDDED_OBJECT =>
        throw new RuntimeException("We weren't reading an object, something went wrong")
    }

    maybeValue match {
      case Some(v) if nextContext.isEmpty => v
      case _ =>
        parser.nextToken()
        val toPass = maybeValue.map { v =>
          val previous +: stack = nextContext
          previous.addValue(v) +: stack
        }.getOrElse(nextContext)

        deserialize(parser, ctx, toPass)
    }
  }

  // This is used when the root object is null, ie when deserializing "null"
  override val getNullValue: JsNull.type = JsNull

}

/**
  * Deserializer context.
  */
private[json] sealed trait DeserializerContext {

  def addValue(value: Json): DeserializerContext

}

/**
  * Deserializer array context.
  */
private[json] final case class ReadingList(content: mutable.ArrayBuffer[Json]) extends DeserializerContext {

  def addValue(value: Json): DeserializerContext = ReadingList {
    content += value
  }

}

/**
  * Deserializer map key context.
  */
private[json] final case class KeyRead(content: mutable.Map[String, Json], fieldName: String) extends DeserializerContext {

  def addValue(value: Json): DeserializerContext = ReadingMap {
    content += (fieldName -> value)
  }

}

/**
  * Deserializer map context.
  */
private[json] final case class ReadingMap(content: mutable.Map[String, Json]) extends DeserializerContext {

  def setField(fieldName: String): DeserializerContext = KeyRead(content, fieldName)

  def addValue(value: Json): DeserializerContext = throw new Exception("Cannot add a value on an object without a key, malformed Json object!")

}
