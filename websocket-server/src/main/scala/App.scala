import java.util.concurrent.Executors

import akka.stream.{ActorMaterializer, Graph, SourceShape}
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import cats.effect.{ExitCode, IO, IOApp, Resource}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.settings.ServerSettings
import akka.io.Inet
import akka.stream.scaladsl.Sink

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import cats.implicits._

object App extends IOApp {

  def system: IO[ActorSystem] = IO(ActorSystem())

  def runServerAndSystem(route: Route, cfg: ServerConfig)(implicit system: ActorSystem): IO[Unit] = {
    val defaultSettings = ServerSettings(system)
    val customSettings = defaultSettings
      .withSocketOptions(
        List(Inet.SO.SendBufferSize(cfg.sendBufferSize)))
      .withTimeouts(
        defaultSettings
          .timeouts
          .withIdleTimeout(cfg.idleTimeoutS.seconds)
          .withLingerTimeout(cfg.lingerTimeoutS.seconds))

    for {
      binding <- IO.fromFuture(IO {
        implicit val mat = ActorMaterializer()
        Http().bindAndHandle(
          handler = route,
          interface = cfg.httpServer.interface,
          port = cfg.httpServer.port,
          settings = customSettings
        )
      })
      res <- IO(println(binding))
    } yield res
  }

  def route(cfg: ServerConfig, source: Graph[SourceShape[Message], Any]) = path(cfg.route) {
    extractUpgradeToWebSocket { upgrade =>
      complete({
        upgrade.handleMessagesWithSinkSource(Sink.ignore, source)
      })
    }
  }

  val blocking: Resource[IO, ExecutionContext] =
    Resource
      .make(IO(Executors.newCachedThreadPool()))(exec => IO(exec.shutdown()))
      .map(ExecutionContext.fromExecutor)

  def run(args: List[String]): IO[ExitCode] =
    blocking.use { blockingCS =>
      for {
        cfg <- ServerConfig.read
        /*implicit0*/(sys: ActorSystem) <- system
        source <- DataSource.getDataSource(cfg, ETFTickerMapper.apply, blockingCS)
        _ <- runServerAndSystem(route(cfg, source), cfg)(sys)
        _ <- IO.never
      } yield ExitCode.Success
    }

}
