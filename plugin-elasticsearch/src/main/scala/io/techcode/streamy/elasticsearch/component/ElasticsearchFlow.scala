/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018-2020
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

import java.util.concurrent.ThreadLocalRandom
import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{ContentType, _}
import akka.stream._
import akka.stream.scaladsl.Flow
import akka.stream.stage._
import akka.util.ByteString
import com.google.common.base.Throwables
import io.techcode.streamy.elasticsearch.event._
import io.techcode.streamy.event.{AttributeKey, StreamEvent}
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._
import pureconfig._
import pureconfig.generic.semiauto._

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * Elasticsearch flow companion.
  */
object ElasticsearchFlow {

  // Default values
  val DefaultBulk: Int = 100
  val DefaultRetry: RetryConfig = RetryConfig(
    minBackoff = 100 millis,
    maxBackoff = 5 second,
    randomFactor = 0.3
  )
  val DefaultFlushTimeout: FiniteDuration = 10 seconds
  val DefaultBinding: Binding = Binding()

  // New line delimiter
  private val NewLineDelimiter: ByteString = ByteString("\n")

  // Attribute keys
  val DocIdKey: AttributeKey[String] = AttributeKey("elasticsearch-doc-id")
  val DocVersionKey: AttributeKey[Long] = AttributeKey("elasticsearch-doc-version")
  val DocVersionTypeKey: AttributeKey[String] = AttributeKey("elasticsearch-doc-version-type")
  val DocTypeKey: AttributeKey[String] = AttributeKey("elasticsearch-doc-type")
  val IndexNameKey: AttributeKey[String] = AttributeKey("elasticsearch-index-name")

  // Configuration readers
  implicit val basicAuthConfig: ConfigReader[BasicAuthConfig] = deriveReader[BasicAuthConfig]

  implicit val authConfigReader: ConfigReader[AuthConfig] = ConfigReader.fromFunction[AuthConfig](conf =>
    basicAuthConfig.from(conf)
  )
  implicit val errorHandlerReader: ConfigReader[() => ErrorHandler] = ConfigReader.fromString[() => ErrorHandler](_ =>
    Right(() => DefaultErrorHandler)
  )
  implicit val retryConfigReader: ConfigReader[RetryConfig] = deriveReader[RetryConfig]

  implicit val bindingConfigReader: ConfigReader[Binding] = deriveReader[Binding]

  implicit val hostConfigReader: ConfigReader[HostConfig] = deriveReader[HostConfig]

  implicit val configReader: ConfigReader[Config] = deriveReader[Config]

  // Relevant http status
  private object HttpStatus {
    val BadRequest: Int = 400
    val NotFound: Int = 404
    val Conflict: Int = 409
    val TooManyRequest: Int = 429
  }

  // Precomputed path
  private object ElasticPath {
    val Document: JsonPointer = Root
    val Errors: JsonPointer = Root / "errors"
    val Items: JsonPointer = Root / "items"
  }

  // Precomputed elasticsearch headers
  private object ElasticHeaders {
    val Id: String = "_id"
    val Index: String = "_index"
    val Type: String = "_type"
    val Version: String = "_version"
    val VersionType: String = "_version_type"
    val Ct: ContentType.WithFixedCharset =
      ContentType(MediaType.customWithFixedCharset("application", "x-ndjson", HttpCharsets.`UTF-8`))
  }

  // Component configuration
  case class Config(
    hosts: Seq[HostConfig],
    indexName: String,
    action: String,
    bulk: Int = DefaultBulk,
    retry: RetryConfig = DefaultRetry,
    binding: Binding = DefaultBinding,
    onError: () => ErrorHandler = () => DefaultErrorHandler,
    flushTimeout: FiniteDuration = DefaultFlushTimeout,
    bypassDocumentParsing: Boolean = false
  )

  // Binding configuration
  case class Binding(
    document: JsonPointer = ElasticPath.Document
  )

  // Retry configuration
  case class RetryConfig(
    minBackoff: FiniteDuration,
    maxBackoff: FiniteDuration,
    randomFactor: Double
  ) {
    def next(retryNo: Int): FiniteDuration = {
      val rnd = 1.0 + ThreadLocalRandom.current().nextDouble() * randomFactor
      val calculatedDuration = Try(maxBackoff.min(minBackoff * math.pow(2, retryNo)) * rnd).getOrElse(maxBackoff)
      calculatedDuration match {
        case f: FiniteDuration => f
        case _ => maxBackoff
      }
    }
  }

  // Error handler
  trait ErrorHandler {

    /**
      * Error event definition.
      *
      * @param status http error status.
      * @param event  current stream event to handle.
      * @param result result of the elasticsearch action.
      * @return error handling strategy.
      */
    def onError(status: Int, event: StreamEvent, result: Json): MaybeJson = JsUndefined

  }

  // Default error handler
  case object DefaultErrorHandler extends ErrorHandler

  // Host configuration
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
  ): Flow[StreamEvent, StreamEvent, NotUsed] =
    {
      if (config.flushTimeout == Duration.Zero) {
        Flow[StreamEvent].batch(config.bulk, immutable.Seq(_)) { case (seq, wm) => seq :+ wm }
      } else {
        Flow[StreamEvent].groupedWithin(config.bulk, config.flushTimeout)
      }
    }
    .via(Flow.fromGraph(new ElasticsearchFlowStage(config)))

  /**
    * Elasticsearch flow stage.
    *
    * @param config flow stage configuration.
    */
  private class ElasticsearchFlowStage(config: Config)(
    implicit system: ActorSystem,
    executionContext: ExecutionContext
  ) extends GraphStage[FlowShape[Seq[StreamEvent], StreamEvent]] {

    // Inlet
    val in: Inlet[Seq[StreamEvent]] = Inlet("ElasticsearchFlow.in")

    // Outlet
    val out: Outlet[StreamEvent] = Outlet("ElasticsearchFlow.out")

    // Shape
    override val shape: FlowShape[Seq[StreamEvent], StreamEvent] = FlowShape.of(in, out)

    // Binding
    val binding: Binding = config.binding

    // Logic generator
    override def createLogic(attr: Attributes): TimerGraphStageLogic = new ElasticsearchFlowLogic

    /**
      * Marshal all events.
      *
      * @param events events to process.
      * @return prepared request in bulk format.
      */
    private def marshalMessages(events: Seq[StreamEvent]): ByteString = events.map(marshalMessage).reduce((x, y) => x ++ y)

    /**
      * Marshal a single packet.
      *
      * @param event event to marshal.
      * @return bytestring representation.
      */
    private def marshalMessage(event: StreamEvent): ByteString = {
      // Retrive header information
      val payload = event.payload
      val id = event.attribute(DocIdKey)
      val `type` = event.attribute(DocTypeKey).getOrElse[String]("doc")
      val version = event.attribute(DocVersionKey)
      val versionType = event.attribute(DocVersionKey)
      val index = event.attribute(IndexNameKey).getOrElse[String](config.indexName)

      // Build header
      val header = Json.obj(
        config.action -> {
          val builder = Json.objectBuilder()
            .+=(ElasticHeaders.Index -> index)
            .+=(ElasticHeaders.Type -> `type`)

          // Add version if present
          version.foreach(x => builder += (ElasticHeaders.Version -> x))

          // Add version type if present
          versionType.foreach(x => builder += (ElasticHeaders.VersionType -> x))

          // Add id if present
          id.foreach(x => builder += (ElasticHeaders.Id -> x))
          builder.result()
        }
      )

      // Retrieve document
      val doc = {
        val analysis = payload.evaluate(binding.document).get[Json]
        if (analysis.isBytes && config.bypassDocumentParsing) {
          analysis.get[ByteString]
        } else {
          Json.printByteStringUnsafe(analysis)
        }
      }
      Json.printByteStringUnsafe(header) ++ NewLineDelimiter ++ doc ++ NewLineDelimiter
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

      // Next retry
      private var retryNo: Int = 0

      // Error behaviour handling
      val errorBehaviour: ErrorHandler = config.onError()

      // State
      object State extends Enumeration {
        val Idle, Busy = Value
      }

      // List of hosts to use
      private val hosts: Iterator[HostConfig] = LazyList.continually(config.hosts.to(LazyList)).flatten.iterator

      // Pending message
      private var messages: Seq[StreamEvent] = Nil

      // Current processing message
      private var inProcessMessages: Seq[StreamEvent] = Nil

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
      @inline def handleFailure(ex: Throwable): Unit = processFailure(Throwables.getStackTraceAsString(ex))

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
        * Schedule a retry.
        */
      def scheduleRetry(): Unit = {
        val nextRetry: FiniteDuration = config.retry.next(retryNo)
        retryNo += 1
        scheduleOnce(NotUsed, nextRetry)
      }

      /**
        * Emit messages in order downstream.
        */
      def emitDownstream(): Unit = {
        val results = messages
        messages = Nil
        inProcessMessages = Nil
        retryNo = 0
        state = State.Idle
        emitMultiple(out, results.iterator, () => checkForCompletion())
      }

      /**
        * Process success elements.
        */
      def processSuccess(): Unit = {
        system.eventStream.publish(ElasticsearchEvent.Success(elapsed(), messages.size))
        emitDownstream()
      }

      /**
        * Process partial elements.
        */
      def processPartial(data: Json): Unit = {
        // Handle failed items
        val items = data.evaluate(ElasticPath.Items).get[JsArray]

        // Flag for back pressure
        var backPressure = false

        // Create a list of documents to reprocess
        val partialDocuments = Vector.newBuilder[StreamEvent]
        partialDocuments.sizeHint(inProcessMessages.size)

        // Map with result and process
        inProcessMessages.zip(items.toSeq)
          .foreach { case (document, result) =>
            val status = result.evaluate(statusPath).get[Int]

            // We delegate to error behaviour in case of conflict or bad request or not found
            status match {
              case HttpStatus.Conflict | HttpStatus.BadRequest | HttpStatus.NotFound =>
                val resultBehaviour = errorBehaviour.onError(status, document, result)
                if (resultBehaviour.isEmpty) {
                  system.eventStream.publish(ElasticsearchEvent.Drop(document, result))
                } else {
                  partialDocuments += document.mutate(document.payload
                    .patch(Replace(binding.document, resultBehaviour.get[Json])).get[Json]
                  )
                }
              case HttpStatus.TooManyRequest =>
                backPressure = true
                partialDocuments += document
              case _ => partialDocuments += document
            }
          }

        // Compute reprocess next request
        inProcessMessages = partialDocuments.result()
        if (inProcessMessages.nonEmpty) {
          if (backPressure) {
            scheduleRetry()
          } else {
            retryNo = 0
            onTimer(NotUsed)
          }
        } else {
          emitDownstream()
        }
        system.eventStream.publish(ElasticsearchEvent.Partial(elapsed(), inProcessMessages.size))
      }

      /**
        * Process failure elements.
        *
        * @param exceptionMsg exception message.
        */
      def processFailure(exceptionMsg: String): Unit = {
        system.eventStream.publish(ElasticsearchEvent.Failure(exceptionMsg, elapsed()))
        scheduleRetry()
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
            .withEntity(ElasticHeaders.Ct, marshalMessages(inProcessMessages))

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

          // Send request
          Http().singleRequest(request).onComplete {
            case Success(response) => response match {
              case HttpResponse(StatusCodes.OK, _, entity, _) =>
                entity.dataBytes.runFold(ByteString.empty)(_ ++ _).foreach { x =>
                  successHandler.invoke(Json.parseByteStringUnsafe(x))
                }
              case HttpResponse(_, _, _, _) =>
                try {
                  failureHandler.invoke(new StreamException(StreamEvent.Empty, response.httpMessage.toString))
                } finally {
                  response.discardEntityBytes()
                }
            }
            case Failure(exception) =>
              failureHandler.invoke(new StreamException(StreamEvent.Empty, exception))
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
