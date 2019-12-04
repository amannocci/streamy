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
package io.techcode.streamy.elasticsearch.component

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler, StageLogging}
import akka.stream.{Attributes, Outlet, OverflowStrategy, SourceShape}
import akka.util.ByteString
import com.softwaremill.sttp._
import io.techcode.streamy.elasticsearch.event.ElasticsearchEvent
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Elasticsearch source companion.
  */
object ElasticsearchSource {

  // Default values
  val DefaultBulk = 500

  // Precomputed path
  private object ElasticPath {
    val Hits: JsonPointer = Root / "hits" / "hits"
    val ScrollId: JsonPointer = Root / "_scroll_id"
    val Size: JsonPointer = Root / "size"
  }

  // Component configuration
  case class Config(
    hosts: Seq[String],
    indexName: String,
    query: Json,
    bulk: Int = DefaultBulk
  )

  /**
    * Create a new elasticsearch single source.
    *
    * @param config source configuration.
    */
  def single(config: Config)(
    implicit httpClient: SttpBackend[Future, Source[ByteString, NotUsed]],
    system: ActorSystem,
    executionContext: ExecutionContext
  ): Source[ByteString, Future[NotUsed]] = {
    val hosts = LazyList.continually(config.hosts.to(LazyList)).flatten.iterator

    // Retrieve uri based on scroll id
    val uri = uri"${hosts.next()}/${config.indexName}/_search"

    // Prepare request
    var request = sttp.post(uri)
      .header("Content-Type", "application/json")
      .readTimeout(5 seconds)
      .body(Json.printStringUnsafe(config.query))
      .response(asStream[Source[ByteString, NotUsed]])

    // Add basic auth
    request = if (uri.userInfo.isDefined) {
      val userInfo = uri.userInfo.get
      request.auth.basic(userInfo.username, userInfo.password.get)
    } else {
      request
    }

    // Add request body
    Source.fromFutureSource(request.send().flatMap { response =>
      response.body match {
        case Left(ex) => Future.failed(new StreamException(ex))
        case Right(data) => Future.successful(data)
      }
    })
  }

  /**
    * Create a new elasticsearch paginate source.
    *
    * @param config source configuration.
    * @return source.
    */
  def paginate(config: Config)(
    implicit httpClient: SttpBackend[Future, Source[ByteString, NotUsed]],
    system: ActorSystem,
    executionContext: ExecutionContext
  ): Source[Json, NotUsed] =
    Source.fromGraph(new ElasticsearchPaginateSourceStage(config))
      .buffer(config.bulk, OverflowStrategy.backpressure)

  /**
    * Elasticsearch paginate source stage.
    *
    * @param config source stage configuration.
    */
  private class ElasticsearchPaginateSourceStage(config: Config)(
    implicit httpClient: SttpBackend[Future, Source[ByteString, NotUsed]],
    system: ActorSystem,
    executionContext: ExecutionContext
  ) extends GraphStage[SourceShape[Json]] {

    // Outlet
    val out: Outlet[Json] = Outlet("ElasticsearchPaginateSource.out")

    // Shape
    override val shape: SourceShape[Json] = SourceShape(out)

    // Logic generator
    override def createLogic(attr: Attributes): GraphStageLogic = new ElasticsearchPaginateSourceLogic

    /**
      * Elasticsearch paginate source logic.
      */
    private class ElasticsearchPaginateSourceLogic extends GraphStageLogic(shape) with OutHandler with StageLogging {

      // Current scroll id context
      private var scrollId: MaybeJson = JsUndefined

      // Set handler
      setHandler(out, this)

      // Async success handler
      private val successHandler = getAsyncCallback[Response[Json]](handleSuccess)

      // Async failure handler
      private val failureHandler = getAsyncCallback[Throwable](handleFailure)

      // List of hosts to use
      private val hosts: Iterator[String] = LazyList.continually(config.hosts.to(LazyList)).flatten.iterator

      // Start request time
      private var started: Long = System.currentTimeMillis()

      /**
        * Handle request success.
        *
        * @param response http response.
        */
      def handleSuccess(response: Response[Json]): Unit = {
        response.body match {
          case Left(ex) => handleFailure(new StreamException(ex))
          case Right(data) =>
            // Retrieve hits
            val result = data.evaluate(ElasticPath.Hits)
            if (result.isDefined) {
              system.eventStream.publish(ElasticsearchEvent.Success(elapsed()))

              // Check if we have at least one hit
              val it = result.get[JsArray].iterator
              if (it.hasNext) {
                scrollId = data.evaluate(ElasticPath.ScrollId)
                emitMultiple(out, it)
              } else {
                completeStage()
              }
            } else {
              handleFailure(new StreamException(response.statusText))
            }
        }
      }

      /**
        * Handle request failure.
        *
        * @param ex request exception.
        */
      def handleFailure(ex: Throwable): Unit = {
        system.eventStream.publish(ElasticsearchEvent.Failure(ex.getMessage, elapsed()))
        failStage(ex)
      }

      /**
        * Gets time elapsed between begin of request and now.
        */
      def elapsed(): Long = System.currentTimeMillis() - started

      val asJson: ResponseAs[Json, Nothing] = asByteArray.map(Json.parseBytesUnsafe)

      override def onPull(): Unit = {
        {
          // Retrieve uri based on scroll id
          val uri = if (scrollId.isEmpty) {
            uri"${hosts.next()}/${config.indexName}/_search?scroll=5m&sort=_doc"
          } else {
            uri"${hosts.next()}/_search/scroll"
          }

          // Prepare request
          var request = sttp.post(uri)
            .header("Content-Type", "application/json")
            .readTimeout(5 seconds)
            .response(asJson)


          request = if (scrollId.isEmpty) {
            request.body(Json.printStringUnsafe(config.query.patch(Add(ElasticPath.Size, config.bulk)).get[Json]))
          } else {
            request.body(Json.printStringUnsafe(Json.obj(
              "scroll" -> "5m",
              "scroll_id" -> scrollId.get[Json]
            )))
          }


          started = System.currentTimeMillis()


          if (uri.userInfo.isDefined) {
            val userInfo = uri.userInfo.get
            request.auth.basic(userInfo.username, userInfo.password.get).send()
          } else {
            request.send()
          }
          }.onComplete {
          case Success(response) => successHandler.invoke(response)
          case Failure(ex) => failureHandler.invoke(ex)
        }
      }

    }

  }

}
