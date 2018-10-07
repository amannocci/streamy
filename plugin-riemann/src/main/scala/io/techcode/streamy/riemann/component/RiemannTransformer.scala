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
package io.techcode.streamy.riemann.component

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import io.riemann.riemann.Proto
import io.techcode.streamy.protobuf.component.ProtobufTransformer
import io.techcode.streamy.util.json._

/**
  * Riemann transformer companion.
  */
object RiemannTransformer {

  /**
    * Create a riemann flow that transform incoming [[ByteString]] to [[Json]].
    *
    * @param conf flow configuration.
    * @return new riemann flow.
    */
  def parser(conf: Parser.Config): Flow[ByteString, Json, NotUsed] =
    ProtobufTransformer.parser[Proto.Msg](ProtobufTransformer.Parser.Config(
      proto = Proto.Msg.getDefaultInstance,
      maxSize = conf.maxSize,
      decoder = new ParserLogic(conf.binding)
    ))

  /**
    * Create a riemann flow that transform incoming [[Json]] to [[ByteString]].
    *
    * @param conf flow configuration.
    * @return new riemann flow.
    */
  def printer(conf: Printer.Config): Flow[Json, ByteString, NotUsed] =
    ProtobufTransformer.printer[Proto.Msg](ProtobufTransformer.Printer.Config(
      proto = Proto.Msg.getDefaultInstance,
      maxSize = conf.maxSize,
      encoder = new PrinterLogic(conf.binding)
    ))

  private class ParserLogic(binding: Parser.Binding) extends (Proto.Msg => Json) {

    val eventBinding: Parser.EventBinding = binding.event

    // scalastyle:off cyclomatic.complexity
    override def apply(pkt: Proto.Msg): Json = {
      val builder = Json.objectBuilder()
      if (pkt.hasOk) builder.put(binding.ok, pkt.getOk)
      if (pkt.hasError) builder.put(binding.error, pkt.getError)
      if (pkt.getEventsCount > 0) {
        val events = Json.arrayBuilder()
        for (i <- 0 until pkt.getEventsCount) {
          val el = pkt.getEvents(i)
          val evt = Json.objectBuilder()
          if (el.hasTime) evt.put(eventBinding.time, el.getTime)
          if (el.hasState) evt.put(eventBinding.state, el.getState)
          if (el.hasService) evt.put(eventBinding.service, el.getService)
          if (el.hasHost) evt.put(eventBinding.host, el.getHost)
          if (el.hasDescription) evt.put(eventBinding.description, el.getDescription)
          if (el.getTagsCount > 0) {
            val tags = Json.arrayBuilder()
            el.getTagsList.forEach(x => tags.add(x))
            evt.put(eventBinding.tags, tags.result())
          }
          if (el.hasTtl) evt.put(eventBinding.ttl, el.getTtl)
          if (el.getAttributesCount > 0) {
            val attrs = Json.objectBuilder()
            el.getAttributesList.forEach(a => attrs.put(a.getKey, a.getValue))
            evt.put(eventBinding.attributes, attrs.result())
          }
          if (el.hasTimeMicros) evt.put(eventBinding.timeMicros, el.getTimeMicros)
          if (el.hasMetricSint64) evt.put(eventBinding.metricSint64, el.getMetricSint64)
          if (el.hasMetricD) evt.put(eventBinding.metricD, el.getMetricD)
          if (el.hasMetricF) evt.put(eventBinding.metricF, el.getMetricF)
          events.add(evt.result())
        }
        builder.put(binding.events, events.result())
      }
      builder.result()
    }

    // scalastyle:on cyclomatic.complexity

  }

  private class PrinterLogic(binding: Printer.Binding) extends (Json => Proto.Msg) {

    val eventBinding: Printer.EventBinding = binding.event

    // scalastyle:off cyclomatic.complexity
    override def apply(pkt: Json): Proto.Msg = {
      val builder = Proto.Msg.newBuilder()

      pkt.evaluate(binding.ok).asBoolean.foreach(builder.setOk)
      pkt.evaluate(binding.error).asString.foreach(builder.setError)
      pkt.evaluate(binding.events).asArray.foreach { evts =>
        val it = evts.toIterator
        while (it.hasNext) {
          val event = Proto.Event.newBuilder()
          it.next().asObject.foreach { evt =>
            evt.evaluate(eventBinding.time).asLong.foreach(event.setTime)
            evt.evaluate(eventBinding.state).asString.foreach(event.setState)
            evt.evaluate(eventBinding.service).asString.foreach(event.setService)
            evt.evaluate(eventBinding.host).asString.foreach(event.setHost)
            evt.evaluate(eventBinding.description).asString.foreach(event.setDescription)
            evt.evaluate(eventBinding.tags).asArray.foreach { el =>
              val tagIt = el.toIterator
              while (tagIt.hasNext) {
                tagIt.next().asString.foreach(event.addTags)
              }
              el.asString.foreach(event.addTags)
            }
            evt.evaluate(eventBinding.ttl).asFloat.foreach(event.setTtl)
            evt.evaluate(eventBinding.attributes).asObject.foreach { el =>
              el.foreach { pair =>
                pair._2 match {
                  case JsString(value) =>
                    event.addAttributes(Proto.Attribute.newBuilder().setKey(pair._1).setValue(value))
                  case x: Json =>
                    event.addAttributes(Proto.Attribute.newBuilder().setKey(pair._1).setValue(x.toString))
                }
              }
            }
            evt.evaluate(eventBinding.timeMicros).asLong.foreach(event.setTimeMicros)
            evt.evaluate(eventBinding.metricSint64).asLong.foreach(event.setMetricSint64)
            evt.evaluate(eventBinding.metricD).asDouble.foreach(event.setMetricD)
            evt.evaluate(eventBinding.metricF).asFloat.foreach(event.setMetricF)
          }
          builder.addEvents(event)
        }
      }
      builder.build()
    }

    // scalastyle:on cyclomatic.complexity

  }

  // Parser related stuff
  object Parser {

    // Binding
    case class Binding(
      ok: String = "ok",
      error: String = "error",
      events: String = "events",
      event: EventBinding = EventBinding()
    )

    // Event binding
    case class EventBinding(
      time: String = "time",
      state: String = "state",
      service: String = "service",
      host: String = "host",
      description: String = "description",
      tags: String = "tags",
      ttl: String = "ttl",
      attributes: String = "attributes",
      timeMicros: String = "time_micros",
      metricSint64: String = "metric_sint64",
      metricD: String = "metric_d",
      metricF: String = "metric_f"
    )

    // Configuration
    case class Config(
      maxSize: Int = Int.MaxValue - 4,
      binding: Binding = Binding()
    )

  }

  // Printer related stuff
  object Printer {

    // Binding
    case class Binding(
      ok: JsonPointer = Root / "ok",
      error: JsonPointer = Root / "error",
      events: JsonPointer = Root / "events",
      event: EventBinding = EventBinding()
    )

    // Event binding
    case class EventBinding(
      time: JsonPointer = Root / "time",
      state: JsonPointer = Root / "state",
      service: JsonPointer = Root / "service",
      host: JsonPointer = Root / "host",
      description: JsonPointer = Root / "description",
      tags: JsonPointer = Root / "tags",
      ttl: JsonPointer = Root / "ttl",
      attributes: JsonPointer = Root / "attributes",
      timeMicros: JsonPointer = Root / "time_micros",
      metricSint64: JsonPointer = Root / "metric_sint64",
      metricD: JsonPointer = Root / "metric_d",
      metricF: JsonPointer = Root / "metric_f"
    )

    // Configuration
    case class Config(
      maxSize: Int = Int.MaxValue - 4,
      binding: Binding = Binding()
    )

  }

}
