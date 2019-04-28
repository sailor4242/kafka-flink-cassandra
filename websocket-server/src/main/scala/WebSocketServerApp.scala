import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.settings.ServerSettings
import akka.io.Inet
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings }
import akka.stream.scaladsl.{ FileIO, Framing, Sink }
import akka.util.ByteString
import cats.effect._
import cats.implicits._
import syntax._
import cats.temp.par.Par
import pureconfig.loadConfigOrThrow
import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import io.circe.generic.auto._
import io.circe.syntax._
import pureconfig.generic.auto._
import io.chrisdavenport.log4cats.Logger

/**
 * Websocket server that reads csv file of stock ticks
 * and sends it via exposed port with some configurable delay
 */
class WebSocketServerApp[F[_]: Timer: ContextShift: Par: LiftIO](logger: Logger[F])(implicit F: ConcurrentEffect[F]) {

  case class Resources(config: ServerConfig, system: ActorSystem)

  implicit val system = ActorSystem(this.getClass.getName.split("\\$").last)
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))
  implicit val executionContext = system.dispatcher

  def resources(system: ActorSystem): Resource[F, Resources] =
    for {
      config <- F.delay(loadConfigOrThrow[ServerConfig]).resource
      system <- Resource.make(F.delay(system))(
        s => LiftIO[F].liftIO(IO.fromFuture(IO(s.terminate()))).void)
    } yield Resources(config, system)

  def launch(r: Resources): F[Unit] = {
    // Config for the input/output
    val iveMapper = new IveTickerMapper()

    for {
      // Generate a file source to read records from (CSV file)
      fileSource <- F.delay(FileIO
        .fromPath(Paths.get(r.config.dataFile))
        .via(Framing.delimiter(ByteString("\n"), Int.MaxValue)))

      // map the filesource via a delayed source and convert the csv into JSON
      delayedSource <- F.delay(fileSource
        .throttle(elements = 1, per = r.config.sleepIntervalMs.millis)
        .map { line =>
          logger.debug(line.utf8String)
          val ticker = iveMapper.map(line)
          TextMessage(ticker.asJson.toString())
        })

      // define a websocket route for clients to connect to and start receiving the stream of ticker items
      route <- F.delay(path(r.config.route) {
        extractUpgradeToWebSocket { upgrade =>
          complete({
            logger.info("Received request for data")
            upgrade.handleMessagesWithSinkSource(Sink.ignore, delayedSource)
          })
        }
      })

      // Define some webserver settings to finetune the websocket server
      defaultSettings <- F.delay(ServerSettings(r.system))
      pingCounter <- F.delay(new AtomicInteger())

      customServerSettings <- F.delay(defaultSettings
        .withWebsocketSettings(
          defaultSettings
            .websocketSettings
            .withPeriodicKeepAliveData(() => ByteString(s"debug-${pingCounter.incrementAndGet()}")))
        .withSocketOptions(
          List(Inet.SO.SendBufferSize(r.config.sendBufferSize)))
        .withTimeouts(
          defaultSettings
            .timeouts
            .withIdleTimeout(r.config.idleTimeoutS.seconds)
            .withLingerTimeout(r.config.lingerTimeoutS.seconds)))

      // Bind the route to the webserver
      _ <- F.delay(Http()(r.system)
        .bindAndHandle(
          handler = route,
          interface = r.config.httpServer.interface,
          port = r.config.httpServer.port,
          settings = customServerSettings)
        .onComplete {
          case Success(_) => logger.info(s"Server is up and running")
          case Failure(e) =>
            logger.error(s"Binding failed with ${e.getMessage}")
            r.system.terminate()
        })
    } yield ()
  }

  def run: Resource[F, Unit] = resources(system).flatMap(r => launch(r).resource)
}

