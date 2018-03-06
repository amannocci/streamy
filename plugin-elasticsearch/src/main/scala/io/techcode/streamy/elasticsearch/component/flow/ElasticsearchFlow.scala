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
package io.techcode.streamy.elasticsearch.component.flow

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.{Balance, Flow, GraphDSL, Merge, Source}
import akka.stream.stage._
import akka.util.ByteString
import com.softwaremill.sttp._
import io.techcode.streamy.util.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Elasticsearch flow companion.
  */
object ElasticsearchFlow {

  // Default values
  val DefaultBulk = 500
  val DefaultWorker = 1

  // Component configuration
  case class Config(
    hosts: Seq[String],
    indexName: String,
    typeName: String,
    action: String,
    bulk: Int = DefaultBulk,
    worker: Int = DefaultWorker
  )

  /**
    * Create a new elasticsearch flow.
    *
    * @param config sink configuration.
    * @return sink.
    */
  def apply(config: Config)(
    implicit httpClient: SttpBackend[Future, Source[ByteString, NotUsed]],
    executionContext: ExecutionContext
  ): Flow[Json, Json, NotUsed] = {
    val grouped = Flow[Json].grouped(config.bulk)

    if (config.worker > 1) {
      grouped.via(balancer(Flow.fromGraph(new ElasticsearchFlowStage(config)), config.worker))
    } else {
      grouped.via(Flow.fromGraph(new ElasticsearchFlowStage(config)))
    }
  }

  /**
    * Balancing jobs to a fixed pool of workers.
    *
    * @param worker      worker logic.
    * @param workerCount worker count.
    * @tparam In  input type.
    * @tparam Out output type.
    * @return balanced flow.
    */
  private def balancer[In, Out](worker: Flow[In, Out, Any], workerCount: Int): Flow[In, Out, NotUsed] = {
    import akka.stream.scaladsl.GraphDSL.Implicits._

    Flow.fromGraph(GraphDSL.create() { implicit b ⇒
      val balancer = b.add(Balance[In](workerCount, waitForAllDownstreams = true))
      val merge = b.add(Merge[Out](workerCount))

      for (_ ← 1 to workerCount) {
        // for each worker, add an edge from the balancer to the worker, then wire
        // it to the merge element
        balancer ~> worker.async ~> merge
      }

      FlowShape(balancer.in, merge.out)
    })
  }

  /**
    * Elasticsearch flow stage.
    *
    * @param config  flow stage configuration.
    * @param backend implicit akka http backend.
    */
  private class ElasticsearchFlowStage(config: Config)(
    implicit val backend: SttpBackend[Future, Source[ByteString, NotUsed]],
    ec: ExecutionContext
  ) extends GraphStage[FlowShape[Seq[Json], Json]] {

    // Inlet
    val in: Inlet[Seq[Json]] = Inlet("ElasticsearchFlow.in")

    // Outlet
    val out: Outlet[Json] = Outlet("ElasticsearchFlow.out")

    // Shape
    override val shape: FlowShape[Seq[Json], Json] = FlowShape.of(in, out)

    // Logic generator
    override def createLogic(attr: Attributes): GraphStageLogic =
      new ElasticsearchFlowLogic

    /**
      * Elasticsearch flow logic.
      */
    private class ElasticsearchFlowLogic extends GraphStageLogic(shape) with InHandler with OutHandler with StageLogging {

      // Set handler
      setHandlers(in, out, this)

      // Async success handler
      private val successHandler = getAsyncCallback[Response[Json]](handleSuccess)

      // Async failure handler
      private val failureHandler = getAsyncCallback[Throwable](handleFailure)

      // State of the logic
      private var state = State.Idle

      // Start request time
      private var started: Long = System.currentTimeMillis()

      // State
      object State extends Enumeration {
        val Idle, Busy = Value
      }

      // List of hosts to use
      private val hosts: Iterator[String] = Stream.continually(config.hosts.toStream).flatten.toIterator

      def metric(isSuccess: Boolean = true): Unit = {
        // Log end of request
        val time = System.currentTimeMillis() - started
        log.info(Json.obj(
          "component" -> "elasticsearch-flow",
          "response_time" -> time,
          "is_success" -> isSuccess
        ))
      }

      /**
        * Handle request success.
        *
        * @param response http response.
        */
      def handleSuccess(response: Response[Json]): Unit = {
        metric()
        response.body match {
          case Left(ex) => handleFailure(new IllegalStateException(ex))
          case Right(data) =>
            val result = data.evaluate(Root / "errors").asBoolean
            if (result.getOrElse(true)) {
              handleFailure(new IllegalStateException(data.toString()))
            } else {
              if (isClosed(in)) {
                completeStage()
              } else {
                state = State.Idle
                pull(in)
              }
            }
        }
      }

      /**
        * Handle request failure.
        *
        * @param ex request exception.
        */
      def handleFailure(ex: Throwable): Unit = {
        metric(isSuccess = false)
        failStage(ex)
      }

      /**
        * Marshal all packets.
        *
        * @param pkts packets to process.
        * @return prepared request in bulk format.
        */
      def marshalMessages(pkts: Seq[Json]): Array[Byte] = {
        pkts.map(marshalMessage)
          .reduce((x, y) => x ++ y)
          .compact.toArray[Byte]
      }

      /**
        * Marshal a single packet.
        *
        * @param pkt packet to marshal.
        * @return bytestring representation.
        */
      def marshalMessage(pkt: Json): ByteString = {
        val id = pkt.evaluate(Root / "_id").asString
        val `type` = pkt.evaluate(Root / "_type").asString.getOrElse(config.typeName)
        val header = Json.obj(config.action -> {
          val builder = Json.objectBuilder()
            .put("_index" -> config.indexName)
            .put("_type" -> `type`)
          if (id.nonEmpty) {
            builder.put("_id" -> id.get)
          }
          builder.result()
        })

        val doc = pkt.patch(Bulk(Root, Seq(
          Remove(Root / "_id", mustExist = false),
          Remove(Root / "_type", mustExist = false)
        ))).get
        ByteString(header.toString()) ++ ByteString("\n") ++ ByteString(doc.toString()) ++ ByteString("\n")
      }

      val asJson: ResponseAs[Json, Nothing] = asByteArray.map(Json.parse(_).getOrElse(JsNull))

      override def onPull(): Unit = pull(in)

      override def onPush(): Unit = {
        // Keep going, we handle stage complete after last response
        setKeepGoing(true)

        // Perform request
        state = State.Busy
        started = System.currentTimeMillis()
        sttp
          .post(uri"${hosts.next()}/_bulk")
          .header("Content-Type", "application/x-ndjson")
          .body(marshalMessages(grab(in)))
          .response(asJson)
          .send()
          .onComplete {
            case Success(response) => successHandler.invoke(response)
            case Failure(ex) => failureHandler.invoke(ex)
          }
      }

      override def onUpstreamFinish(): Unit = {
        // Base on state
        state match {
          case State.Idle =>
            completeStage()
          case State.Busy => ()
        }
      }

    }

  }

}
