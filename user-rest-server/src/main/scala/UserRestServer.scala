
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import akka.stream.{ActorMaterializer, Materializer}
import config.Config
import http.Http
import pureconfig.generic.auto._
import scalaz.zio._
import scalaz.zio.blocking.Blocking
import scalaz.zio.clock.Clock
import scalaz.zio.console._
import scalaz.zio.scheduler.Scheduler
import http._
import repository._
import service._
import config._


object UserRestServer extends App {

  implicit val system: ActorSystem = ActorSystem("akka-http")
  implicit val materializer: Materializer = ActorMaterializer()

  val app = for {
      cfg          <- ZIO.fromEither(pureconfig.loadConfig[Config])
      _            <- initDb(cfg.dbConfig)

      blockingEC   <- ZIO.environment[Blocking].flatMap(_.blocking.blockingExecutor).map(_.asEC)
      transactorR   = mkTransactor(cfg.dbConfig, Platform.executor.asEC, blockingEC)

      server        = http.bindAndHandle(cfg.appConfig.port).flatMap(_ => ZIO.never)
      program      <- transactorR.use { tr =>
        server.provideSome[Environment] { base =>
          new Clock with Console with Blocking with UserService.Live with Http.Live with Repository.Live {
            override protected val transactor: doobie.Transactor[Task] = tr
            override protected val routes: Flow[HttpRequest, HttpResponse, Any] = Routes(userService).routes

            override val scheduler: Scheduler.Service[Any] = base.scheduler
            override val console: Console.Service[Any] = base.console
            override val clock: Clock.Service[Any] = base.clock
            override val blocking: Blocking.Service[Any] = base.blocking
          }
        }
      }
    } yield program

  override def run(args: List[String]): ZIO[UserRestServer.Environment, Nothing, Int] =
    putStrLn("Starting User REST Server ... ") *>
      app.foldM(err => putStrLn(s"Execution failed with: $err") *> ZIO.succeed(1), _ => ZIO.succeed(0))
}
