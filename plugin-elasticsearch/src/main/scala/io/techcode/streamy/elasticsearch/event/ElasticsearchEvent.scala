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
package io.techcode.streamy.elasticsearch.event

import akka.actor.DeadLetterSuppression
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json.Json

/**
  * Elasticsearch event.
  */
object ElasticsearchEvent {

  /**
    * Represent an elasticsearch event.
    */
  trait All extends DeadLetterSuppression

  /**
    * This event is fire when elasticsearch response is a success.
    *
    * @param responseTime      time elapsed between request and response.
    * @param processedElements number of elements processed.
    */
  case class Success(responseTime: Long, processedElements: Int) extends All

  /**
    * This event is fire when elasticsearch response is a partial success.
    *
    * @param responseTime      time elapsed between request and response.
    * @param processedElements number of elements processed.
    */
  case class Partial(responseTime: Long, processedElements: Int) extends All

  /**
    * This event is fire when elasticsearch response is a failure.
    *
    * @param exceptionMsg exception message.
    * @param responseTime time elapsed between request and response.
    */
  case class Failure(exceptionMsg: String, responseTime: Long) extends All

  /**
    * This event is fire when an element is drop due to policy.
    *
    * @param droppedElement droppeed element.
    * @param cause          cause of drop from elasticsearch response.
    */
  case class Drop(droppedElement: StreamEvent, cause: Json) extends All

}
