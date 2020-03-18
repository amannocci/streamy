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
import akka.stream.scaladsl.Flow
import akka.stream.stage._
import akka.util.ByteString
import io.techcode.streamy.elasticsearch.event._
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Elasticsearch flow companion.
  */
object ElasticsearchFlow {

  // Default values
  val DefaultBulk: Int = 500
  val DefaultWorker: Int = 1
  val DefaultRetry: FiniteDuration = 1 second

  // New line delimiter
  private val NewLineDelimiter: ByteString = ByteString("\n")

  // Relevant http status
  private object HttpStatus {
    val BadRequest: Int = 400
    val NotFound: Int = 404
    val Conflict: Int = 409
    val TooManyRequest: Int = 429
  }

  // Precomputed path
  private object ElasticPath {
    val Id: JsonPointer = Root / "_id"
    val Type: JsonPointer = Root / "_type"
    val Version: JsonPointer = Root / "_version"
    val VersionType: JsonPointer = Root / "_version_type"
    val Document: JsonPointer = Root
    val Errors: JsonPointer = Root / "errors"
    val Items: JsonPointer = Root / "items"
  }

  // Component configuration
  case class Config(
    hosts: Seq[HostConfig],
    indexName: String,
    action: String,
    bulk: Int = DefaultBulk,
    retry: FiniteDuration = DefaultRetry,
    binding: Binding = Binding()
  )

  case class Binding(
    id: JsonPointer = ElasticPath.Id,
    `type`: JsonPointer = ElasticPath.Type,
    version: JsonPointer = ElasticPath.Version,
    versionType: JsonPointer = ElasticPath.VersionType,
    document: JsonPointer = ElasticPath.Document
  )

  case class HostConfig(
    scheme: String,
    host: String,
    port: Int,
    auth: Option[AuthConfig] = None
  )

  // Generic auth configuration
  trait AuthConfig

  // Basic auth configuration
  case class BasicAuthConfig(
    username: String,
    password: String
  ) extends AuthConfig

  /**
    * Create a new elasticsearch flow.
    *
    * @param config flow configuration.
    */
  def apply(config: Config)(
    implicit system: ActorSystem,
    executionContext: ExecutionContext
  ): Flow[Json, Json, NotUsed] = Flow[Json]
    .batch(config.bulk, immutable.Seq(_)) { case (seq, wm) => seq :+ wm }
    .via(Flow.fromGraph(new ElasticsearchFlowStage(config)))

  /**
    * Elasticsearch flow stage.
    *
    * @param config flow stage configuration.
    */
  private class ElasticsearchFlowStage(config: Config)(
    implicit system: ActorSystem,
    executionContext: ExecutionContext
  ) extends GraphStage[FlowShape[Seq[Json], Json]] {

    // Inlet
    val in: Inlet[Seq[Json]] = Inlet("ElasticsearchFlow.in")

    // Outlet
    val out: Outlet[Json] = Outlet("ElasticsearchFlow.out")

    // Shape
    override val shape: FlowShape[Seq[Json], Json] = FlowShape.of(in, out)

    // Binding
    val binding: Binding = config.binding

    // Logic generator
    override def createLogic(attr: Attributes): TimerGraphStageLogic = new ElasticsearchFlowLogic

    /**
      * Marshal all packets.
      *
      * @param pkts packets to process.
      * @return prepared request in bulk format.
      */
    private def marshalMessages(pkts: Seq[Json]): ByteString = pkts.map(marshalMessage).reduce((x, y) => x ++ y)

    /**
      * Marshal a single packet.
      *
      * @param pkt packet to marshal.
      * @return bytestring representation.
      */
    private def marshalMessage(pkt: Json): ByteString = {
      // Retrive header information
      val id = pkt.evaluate(ElasticPath.Id)
      val `type` = pkt.evaluate(ElasticPath.Type).getOrElse[String]("doc")
      val version = pkt.evaluate(ElasticPath.Version)
      val versionType = pkt.evaluate(ElasticPath.VersionType)

      // Build header
      val header = Json.obj(
        config.action -> {
          val builder = Json.objectBuilder()
            .+=("_index" -> config.indexName)
            .+=("_type" -> `type`)

          // Add version if present
          version.ifExists[Long] { x =>
            builder += ("_version" -> x)
          }

          // Add version type if present
          versionType.ifExists[String] { x =>
            builder += ("_version_type" -> x)
          }

          // Add id if present
          id.ifExists[String] { x =>
            builder += ("_id" -> x)
          }
          builder.result()
        }
      )

      // Retrieve document
      val doc = pkt.evaluate(binding.document).get[Json]
      Json.printByteStringUnsafe(header) ++ NewLineDelimiter ++ Json.printByteStringUnsafe(doc) ++ NewLineDelimiter
    }

    /**
      * Elasticsearch flow logic.
      */
    private class ElasticsearchFlowLogic extends TimerGraphStageLogic(shape)
      with InHandler with OutHandler with StageLogging {

      // Set handler
      setHandlers(in, out, this)

      // Async success handler
      private val successHandler = getAsyncCallback[Json](handleResponse)

      // Async failure handler
      private val failureHandler = getAsyncCallback[Throwable](handleFailure)

      // State of the logic
      private var state = State.Idle

      // Start request time
      private var started: Long = System.currentTimeMillis()

      // Json pointer to status based on configuration
      private val statusPath: JsonPointer = Root / config.action / "status"

      // State
      object State extends Enumeration {
        val Idle, Busy = Value
      }

      // List of hosts to use
      private val hosts: Iterator[HostConfig] = LazyList.continually(config.hosts.to(LazyList)).flatten.iterator

      // Pending message
      private var messages: Seq[Json] = Nil

      // Current processing message
      private var inProcessMessages: Seq[Json] = Nil

      /**
        * Handle response success.
        *
        * @param data http response data.
        */
      def handleResponse(data: Json): Unit = {
        val errors = data.evaluate(ElasticPath.Errors)
        if (errors.getOrElse[Boolean](true)) {
          processPartial(data)
        } else {
          processSuccess()
        }
      }

      /**
        * Handle request failure.
        *
        * @param ex exception message.
        */
      @inline def handleFailure(ex: Throwable): Unit = processFailure(ex.getMessage)

      /**
        * Returns true if there is no messages to process.
        *
        * @return true if there is no messages to process, otherwise false.
        */
      def checkForCompletion(): Unit =
        if (isClosed(in) && messages.isEmpty && inProcessMessages.isEmpty) {
          completeStage()
        }

      /**
        * Emit messages in order downstream.
        */
      def emitDownstream(): Unit = {
        val results = messages
        messages = Nil
        inProcessMessages = Nil
        state = State.Idle
        emitMultiple(out, results.iterator, () => checkForCompletion)
      }

      /**
        * Process success elements.
        */
      def processSuccess(): Unit = {
        system.eventStream.publish(ElasticsearchEvent.Success(elapsed()))
        emitDownstream()
      }

      /**
        * Process partial elements.
        */
      def processPartial(data: Json): Unit = {
        // Handle failed items
        val items = data.evaluate(ElasticPath.Items).get[JsArray]

        var backPressure = false
        val partialDocuments = inProcessMessages.zip(items.toSeq)
          .filter { case (document, result) =>
            val status = result.evaluate(statusPath).get[Int]

            // We can't do anything in case of conflict or bad request or not found
            status match {
              case HttpStatus.Conflict | HttpStatus.BadRequest | HttpStatus.NotFound =>
                system.eventStream.publish(ElasticsearchEvent.Drop(document, result))
                false
              case HttpStatus.TooManyRequest =>
                backPressure = true
                true
              case _ => true
            }
          }
          .groupBy { case (_, result) =>
            val status = result.evaluate(statusPath).get[Int]
            if (status < 300) 0 else 1
          }

        inProcessMessages = partialDocuments.getOrElse(1, Nil).map(_._1)
        if (inProcessMessages.nonEmpty) {
          if (backPressure) {
            scheduleOnce(NotUsed, config.retry)
          } else {
            state = State.Idle
            performRequest()
          }
        } else {
          emitDownstream()
        }
        system.eventStream.publish(ElasticsearchEvent.Partial(elapsed()))
      }

      /**
        * Process failure elements.
        *
        * @param exceptionMsg exception message.
        */
      def processFailure(exceptionMsg: String): Unit = {
        system.eventStream.publish(ElasticsearchEvent.Failure(exceptionMsg, elapsed()))
        scheduleOnce(NotUsed, config.retry)
      }

      /**
        * Perform a request if idle.
        */
      def performRequest(): Unit = {
        if (state == State.Idle && inProcessMessages.nonEmpty) {
          // Perform request
          state = State.Busy
          started = System.currentTimeMillis()

          // Host configuration
          val hostConf = hosts.next()

          // Prepare request
          var request = HttpRequest()
            .withMethod(HttpMethods.POST)
            .withUri(s"${hostConf.scheme}://${hostConf.host}:${hostConf.port}/_bulk")
            .withEntity(ContentType(MediaType.customWithFixedCharset("application", "x-ndjson", HttpCharsets.`UTF-8`)), marshalMessages(inProcessMessages))

          // Add basic auth
          request = if (hostConf.auth.isDefined) {
            hostConf.auth.get match {
              case BasicAuthConfig(username, password) =>
                request.withHeaders(Seq(
                  Authorization(BasicHttpCredentials(username, password))
                )
                )
              case _ => request
            }
          } else {
            request
          }

          // Send request
          Http().singleRequest(request).map {
            case HttpResponse(StatusCodes.OK, _, entity, _) =>
              entity.dataBytes.runFold(ByteString.empty)(_ ++ _)(materializer).map { x =>
                successHandler.invoke(Json.parseByteStringUnsafe(x))
              }
            case resp@HttpResponse(_, _, _, _) =>
              try {
                failureHandler.invoke(new StreamException(resp.httpMessage.toString))
              } finally {
                resp.discardEntityBytes()(materializer)
              }
          }
        }
      }

      /**
        * Gets time elapsed between begin of request and now.
        */
      def elapsed(): Long = System.currentTimeMillis() - started

      // Start pulling on materialize
      override def preStart(): Unit = pull(in)

      // We have schedule a retry or backpressure
      override def onTimer(timerKey: Any): Unit = {
        state = State.Idle
        performRequest()
      }

      // On demand try to pull
      override def onPull(): Unit =
        if (!isClosed(in) && !hasBeenPulled(in)) {
          pull(in)
        }

      override def onPush(): Unit = {
        // Keep going, we handle stage complete after last response
        setKeepGoing(true)

        // Get element
        messages = grab(in)
        inProcessMessages = messages

        // Perform request if idle
        performRequest()
      }

      override def onUpstreamFinish(): Unit =
      // Base on state
        state match {
          case State.Idle => completeStage()
          case State.Busy => ()
        }

    }

  }

}
