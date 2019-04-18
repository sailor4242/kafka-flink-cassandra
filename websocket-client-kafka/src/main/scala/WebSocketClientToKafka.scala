import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.{ Message, WebSocketRequest }
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ ActorMaterializer, OverflowStrategy }
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringSerializer }
import pureconfig.loadConfigOrThrow
import pureconfig.generic.auto._
import io.circe.parser.decode
import model.Ticker

import scala.concurrent.Promise
import scala.concurrent.duration._

/**
 * Websocket client that connects to the Websocket server and passes all messages to a kafka topic
 * with a backpressure buffer strategy
 */
object WebSocketClientToKafka extends App {

  // Some general config to get the actor system up and running
  val className = this.getClass.getName.split("\\$").last
  implicit val actorSystem = ActorSystem(className)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher
  implicit val config: ClientConfig = loadConfigOrThrow[ClientConfig]
  implicit val logger = Logger(className)

  // Define the Kafka producer
  val producerSettings = ProducerSettings(actorSystem, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers(config.kafkaConfig.port.toString)

  // Create sink that takes messages and creates producer records on the kafka topic
  val kafkaSink: Sink[Message, NotUsed] = Flow[Message]
    .buffer(50, OverflowStrategy.backpressure)
    .via(Flow[Message].throttle(1, 1.second))
    .map {
      case message: Strict =>
        logger.debug(s"ws->kafka: ${message.getStrictText}")
        Some(message.getStrictText)
      case s =>
        logger.debug(s"Skip message: $s")
        None
    }
    .filter(_.isDefined)
    .map(message =>
      new ProducerRecord[Array[Byte], String](decode[Ticker](message.get).right.get.tickerType.entryName, message.get))
    .to(Producer.plainSink(producerSettings))

  // Materialize the sink as a flow
  val flow: Flow[Message, Message, Promise[Option[Message]]] =
    Flow.fromSinkAndSourceMat(kafkaSink, Source.maybe[Message])(Keep.right)

  // Build the websocket server request
  val websocketServerUrlRequest: String = s"ws://${config.host}:${config.port}/"

  // Do the websocket server request
  val (upgradeResponse, promise) =
    Http().singleWebSocketRequest(WebSocketRequest(websocketServerUrlRequest), flow)

}
