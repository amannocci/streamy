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
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.stream._
import akka.stream.scaladsl.Source
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler, StageLogging}
import akka.util.ByteString
import io.techcode.streamy.elasticsearch.event.ElasticsearchEvent
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

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
    hosts: Seq[HostConfig],
    indexName: String,
    query: Json,
    bulk: Int = DefaultBulk
  )

  case class HostConfig(
    scheme: String,
    host: String,
    port: Int,
    auth: Option[AuthConfig] = None
  )

  trait AuthConfig

  case class BasicAuthConfig(
    username: String,
    password: String
  ) extends AuthConfig

  /**
    * Create a new elasticsearch single source.
    *
    * @param config source configuration.
    */
  def single(config: Config)(
    implicit materializer: Materializer,
    system: ActorSystem,
    executionContext: ExecutionContext
  ): Source[StreamEvent, NotUsed] = {
    val hosts = LazyList.continually(config.hosts.to(LazyList)).flatten.iterator

    // Retrieve uri based on scroll id
    val hostConf = hosts.next()
    val uri = s"${hostConf.scheme}://${hostConf.host}:${hostConf.port}/${config.indexName}/_search"

    // Prepare request
    var request = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(uri)
      .withEntity(ContentTypes.`application/json`, Json.printStringUnsafe(config.query))

    // Add basic auth
    request = if (hostConf.auth.isDefined) {
      hostConf.auth.get match {
        case BasicAuthConfig(username, password) =>
          request.withHeaders(Seq(
            Authorization(BasicHttpCredentials(username, password))
          ))
        case _ => request
      }
    } else {
      request
    }

    Source.fromFuture(Http().singleRequest(request).flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.dataBytes.runFold(ByteString.empty)(_ ++ _)(materializer).map { x =>
          StreamEvent(Json.parseByteStringUnsafe(x))
        }
      case resp@HttpResponse(_, _, _, _) =>
        try {
          Future.successful(StreamEvent.Empty.discard(resp.httpMessage.toString))
        } finally {
          resp.discardEntityBytes()(materializer)
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
    implicit system: ActorSystem,
    executionContext: ExecutionContext
  ): Source[StreamEvent, NotUsed] =
    Source.fromGraph(new ElasticsearchPaginateSourceStage(config))
      .buffer(config.bulk, OverflowStrategy.backpressure)

  /**
    * Elasticsearch paginate source stage.
    *
    * @param config source stage configuration.
    */
  private class ElasticsearchPaginateSourceStage(config: Config)(
    implicit system: ActorSystem,
    executionContext: ExecutionContext
  ) extends GraphStage[SourceShape[StreamEvent]] {

    // Outlet
    val out: Outlet[StreamEvent] = Outlet("ElasticsearchPaginateSource.out")

    // Shape
    override val shape: SourceShape[StreamEvent] = SourceShape(out)

    // Logic generator
    override def createLogic(attrs: Attributes): GraphStageLogic = new ElasticsearchPaginateSourceLogic

    /**
      * Elasticsearch paginate source logic.
      */
    private class ElasticsearchPaginateSourceLogic extends GraphStageLogic(shape) with OutHandler with StageLogging {

      // Current scroll id context
      private var scrollId: MaybeJson = JsUndefined

      // Set handler
      setHandler(out, this)

      // Async success handler
      private val successHandler = getAsyncCallback[Json](handleSuccess)

      // Async failure handler
      private val failureHandler = getAsyncCallback[Throwable](handleFailure)

      // List of hosts to use
      private val hosts: Iterator[HostConfig] = LazyList.continually(config.hosts.to(LazyList)).flatten.iterator

      // Start request time
      private var started: Long = System.currentTimeMillis()

      /**
        * Handle request success.
        *
        * @param data http response data.
        */
      def handleSuccess(data: Json): Unit = {
        // Retrieve hits
        val result = data.evaluate(ElasticPath.Hits)
        if (result.isDefined) {
          system.eventStream.publish(ElasticsearchEvent.Success(elapsed()))

          // Check if we have at least one hit
          val it = result.get[JsArray].iterator.map[StreamEvent](StreamEvent(_))
          if (it.hasNext) {
            scrollId = data.evaluate(ElasticPath.ScrollId)
            emitMultiple(out, it)
          } else {
            completeStage()
          }
        } else {
          handleFailure(new StreamException(StreamEvent.Empty, "Response doesn't contains hits field"))
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

      override def onPull(): Unit = {
        // Host configuration
        val hostConf = hosts.next()

        // Retrieve uri based on scroll id
        val uri = if (scrollId.isEmpty) {
          s"${hostConf.scheme}://${hostConf.host}:${hostConf.port}/${config.indexName}/_search?scroll=5m&sort=_doc"
        } else {
          s"${hostConf.scheme}://${hostConf.host}:${hostConf.port}/_search/scroll"
        }

        // Prepare request
        var request = HttpRequest()
          .withMethod(HttpMethods.POST)
          .withUri(uri)

        request = if (scrollId.isEmpty) {
          request.withEntity(ContentTypes.`application/json`, Json.printStringUnsafe(config.query.patch(Add(ElasticPath.Size, config.bulk)).get[Json]))
        } else {
          request.withEntity(ContentTypes.`application/json`, Json.printStringUnsafe(Json.obj(
            "scroll" -> "5m",
            "scroll_id" -> scrollId.get[Json]
          )))
        }

        started = System.currentTimeMillis()

        request = if (hostConf.auth.isDefined) {
          hostConf.auth.get match {
            case BasicAuthConfig(username, password) =>
              request.withHeaders(Seq(
                Authorization(BasicHttpCredentials(username, password))
              ))
            case _ => request
          }
        } else {
          request
        }

        Http().singleRequest(request).map {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            entity.dataBytes.runFold(ByteString.empty)(_ ++ _)(materializer).map { x =>
              successHandler.invoke(Json.parseByteStringUnsafe(x))
            }
          case resp@HttpResponse(_, _, _, _) =>
            try {
              failureHandler.invoke(new StreamException(StreamEvent.Empty, resp.httpMessage.toString))
            } finally {
              resp.discardEntityBytes()(materializer)
            }
        }
      }
    }

  }

}
