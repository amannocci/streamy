/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2020
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
package io.techcode.streamy.kafka.component

import akka.Done
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.kafka.component.KafkaSource.{AutoOffsetReset, Binding}
import io.techcode.streamy.kafka.event.KafkaEvent
import io.techcode.streamy.kafka.util.KafkaSpec
import io.techcode.streamy.util.json.Json

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Kafka sink spec.
  */
class KafkaSinkSpec extends KafkaSpec {

  "Kafka sink" should {
    "publish data using plain sink" in {
      val topic = createTopic()

      val completion = Source.single(StreamEvent(Json.obj("key" -> "key", "value" -> ByteString("value"))))
        .toMat(KafkaSink.plain(KafkaSink.Config(
          bootstrapServers = bootstrapServers,
          topic = topic,
          binding = KafkaSink.Binding(
            key = Some("key"),
            value = Some("value")
          ))))(Keep.right).run()

      whenReady(completion, timeout(120 seconds), interval(100 millis)) { x =>
        x should equal(Done)
      }
    }

    "publish data using committable sink" in {
      val topic = createTopic()
      val groupId = createGroupId()

      system.eventStream.subscribe(testActor, classOf[KafkaEvent.All])

      awaitProduce(produce(topic, 1 to 100))

      val control = KafkaSource.committable(KafkaSource.Config(
        bootstrapServers = bootstrapServers,
        groupId = groupId,
        topics = KafkaSource.StaticTopicConfig(Set(topic)),
        autoOffsetReset = AutoOffsetReset.Earliest,
        binding = KafkaSource.Binding(
          key = Some("key"),
          offset = Some("offset"),
          value = Some("value"),
          partition = Some("partition"),
          topic = Some("topic"),
          timestamp = Some("timestamp")
        )
      )).toMat(KafkaSink.committable(KafkaSink.Config(
        bootstrapServers = bootstrapServers,
        topic = topic,
        binding = KafkaSink.Binding(
          key = Some("key"),
          value = Some("value")
        )), committerDefaults))(DrainingControl.apply).run()

      // Wait for topic consumer and then drain
      receiveOne(1 minute)
      sleep()
      val streamComplete = control.drainAndShutdown()
      whenReady(streamComplete, timeout(120 seconds), interval(100 millis)) { x =>
        receiveOne(1 minute)
        x should equal(Done)
      }
    }
  }

}
