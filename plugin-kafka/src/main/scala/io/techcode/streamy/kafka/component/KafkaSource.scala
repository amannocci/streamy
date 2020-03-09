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

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep}
import akka.util.ByteString
import akka.{Done, NotUsed}
import io.techcode.streamy.component.PassThroughFlow
import io.techcode.streamy.util.json.Json
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

/**
  * Kafka source companion.
  */
object KafkaSource {

  // Component configuration
  case class Config(
    handler: Flow[Json, Json, NotUsed],
    bootstrapServers: String,
    groupId: String,
    autoOffsetReset: String = "earliest",
    topics: Set[String]
  )

  /**
    * Create a new kafka source with at least once semantics.
    *
    * @param config source configuration.
    */
  def atLeastOnce(config: Config)(implicit system: ActorSystem, materializer: Materializer): Consumer.DrainingControl[Done] = {
    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(config.bootstrapServers)
      .withGroupId(config.groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.autoOffsetReset)

    val committerSettings = CommitterSettings(system)

    val process = Flow[ConsumerMessage.CommittableMessage[String, Array[Byte]]]
      .map { msg =>
        val record = msg.record
        Json.obj(
          "key" -> record.key(),
          "offset" -> record.offset(),
          "value" -> ByteString.fromArrayUnsafe(record.value()),
          "partition" -> record.partition(),
          "topic" -> record.topic(),
          "timestamp" -> record.timestamp()
        )
      }

    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(config.topics))
      .via(PassThroughFlow(process.via(config.handler), Keep.right))
      .map(msg => msg.committableOffset)
      .toMat(Committer.sink(committerSettings))(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()
  }

}
