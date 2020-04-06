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

import akka.kafka.ConsumerMessage.CommittableOffset
import akka.stream.scaladsl.Flow
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.kafka.util.KafkaSpec

/**
  * Kafka source spec.
  */
class KafkaSourceSpec extends KafkaSpec {

  "Kafka source" should {
    "retrieve data from topic" in {
      val topic = createTopic()
      val groupId = createGroupId()

      awaitProduce(produceString(topic, Seq("foobar")))
      KafkaSource.atLeastOnce(KafkaSource.Config(
        handler = Flow[StreamEvent[CommittableOffset]],
        bootstrapServers = bootstrapServers,
        groupId = groupId,
        topics = KafkaSource.StaticTopicConfig(Set(topic))
      ))
    }
  }

}
