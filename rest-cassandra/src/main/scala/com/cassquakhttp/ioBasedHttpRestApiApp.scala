package com.cassquakhttp

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.cassquakhttp.http.marketDataHttpRoute
import cats.implicits._
import scala.concurrent.ExecutionContext

object ioBasedHttpRestApiApp extends IOApp {
  println("Starting rest server. Please wait ... ")

  def system: IO[ActorSystem] = IO(ActorSystem("my-system"))

  def runServerAndSystem(route: Route)(implicit system: ActorSystem): IO[Unit] =
    for {
      binding <- IO.fromFuture(IO {
        implicit val mat: ActorMaterializer = ActorMaterializer()
        Http().bindAndHandle(route, "0.0.0.0", 8077)
      })
      res <- IO(println(binding))
    } yield res

  val blocking: Resource[IO, ExecutionContext] =
    Resource
      .make(IO(Executors.newCachedThreadPool()))(exec => IO(exec.shutdown()))
      .map(ExecutionContext.fromExecutor)


  def run(args: List[String]): IO[ExitCode] =
    blocking.use { blockingCS =>
      for {
         sys: ActorSystem <- system
        route = marketDataHttpRoute.route
        _ <- runServerAndSystem(route)(sys)
      } yield ExitCode.Success
    }
  println(
    """
      |you can try queries like :
      | http://127.0.0.1:8077/allData
      | or
      | http://127.0.0.1:8077/data?ticker=FXUS&vol=11
      |  but please wait a little bit ( to server start up )
      |  """.stripMargin )
}
