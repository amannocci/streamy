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
import akka.stream.scaladsl.Sink
import io.techcode.streamy.kafka.component.KafkaSource.{AutoOffsetReset, Binding}
import io.techcode.streamy.kafka.event.KafkaEvent
import io.techcode.streamy.kafka.util.KafkaSpec

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Kafka source spec.
  */
class KafkaSourceSpec extends KafkaSpec {

  "Kafka source" should {
    "retrieve data using plain source" in {
      val topic = createTopic()
      val groupId = createGroupId()
      system.eventStream.subscribe(testActor, classOf[KafkaEvent.All])

      awaitProduce(produce(topic, 1 to 100))

      val control = KafkaSource.plain(KafkaSource.Config(
        bootstrapServers = bootstrapServers,
        groupId = groupId,
        topics = KafkaSource.PatternTopicConfig(topic),
        autoOffsetReset = AutoOffsetReset.Earliest,
        binding = Binding(
          key = Some("key"),
          offset = Some("offset"),
          value = Some("value"),
          partition = Some("partition"),
          topic = Some("topic"),
          timestamp = Some("timestamp")
        )
      )).toMat(Sink.ignore)(DrainingControl.apply)
        .run()

      // Wait for topic consumer and then drain
      receiveOne(1 minute)
      sleep()
      val streamComplete = control.drainAndShutdown()
      whenReady(streamComplete, timeout(120 seconds), interval(100 millis)) { x =>
        receiveOne(1 minute)
        x should equal(Done)
      }
    }

    "retrieve data using plain partitioned source" in {
      val topic = createTopic()
      val groupId = createGroupId()
      system.eventStream.subscribe(testActor, classOf[KafkaEvent.All])

      awaitProduce(produce(topic, 1 to 100))

      val control = KafkaSource.plainPartitioned(KafkaSource.Config(
        bootstrapServers = bootstrapServers,
        groupId = groupId,
        topics = KafkaSource.PatternTopicConfig(topic),
        autoOffsetReset = AutoOffsetReset.Earliest,
        binding = Binding(
          key = Some("key"),
          offset = Some("offset"),
          value = Some("value"),
          partition = Some("partition"),
          topic = Some("topic"),
          timestamp = Some("timestamp")
        )
      )).flatMapMerge(10, _._2)
        .toMat(Sink.ignore)(DrainingControl.apply)
        .run()

      // Wait for topic consumer and then drain
      receiveOne(1 minute)
      sleep()
      val streamComplete = control.drainAndShutdown()
      whenReady(streamComplete, timeout(120 seconds), interval(100 millis)) { x =>
        receiveOne(1 minute)
        x should equal(Done)
      }
    }

    "retrieve data using committable source" in {
      val topic = createTopic()
      val groupId = createGroupId()
      system.eventStream.subscribe(testActor, classOf[KafkaEvent.All])

      awaitProduce(produce(topic, 1 to 100))

      val control = KafkaSource.committable(KafkaSource.Config(
        bootstrapServers = bootstrapServers,
        groupId = groupId,
        topics = KafkaSource.StaticTopicConfig(Set(topic)),
        autoOffsetReset = AutoOffsetReset.Earliest,
        binding = Binding(
          key = Some("key"),
          offset = Some("offset"),
          value = Some("value"),
          partition = Some("partition"),
          topic = Some("topic"),
          timestamp = Some("timestamp")
        )
      )).toMat(KafkaSink.committer())(DrainingControl.apply)
        .run()

      // Wait for topic consumer and then drain
      receiveOne(1 minute)
      sleep()
      val streamComplete = control.drainAndShutdown()
      whenReady(streamComplete, timeout(120 seconds), interval(100 millis)) { x =>
        receiveOne(1 minute)
        x should equal(Done)
      }
    }

    "retrieve data using committable partitioned source" in {
      val topic = createTopic()
      val groupId = createGroupId()
      system.eventStream.subscribe(testActor, classOf[KafkaEvent.All])

      awaitProduce(produce(topic, 1 to 100))

      val control = KafkaSource.committablePartitioned(KafkaSource.Config(
        bootstrapServers = bootstrapServers,
        groupId = groupId,
        topics = KafkaSource.StaticTopicConfig(Set(topic)),
        autoOffsetReset = AutoOffsetReset.Earliest,
        binding = Binding(
          key = Some("key"),
          offset = Some("offset"),
          value = Some("value"),
          partition = Some("partition"),
          topic = Some("topic"),
          timestamp = Some("timestamp")
        )
      )).flatMapMerge(10, _._2)
        .toMat(KafkaSink.committer())(DrainingControl.apply)
        .run()

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
