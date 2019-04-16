package http

import akka.actor.ActorSystem
import akka.http.scaladsl
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import scalaz.zio._

trait Http {
  def http: Http.Service[Any]
}

object Http {

  trait Service[R] {
    def bindAndHandle(port: Int)(
        implicit system: ActorSystem,
        mat: Materializer): ZIO[R, Throwable, ServerBinding]
  }

  trait Live extends Http {

    protected val routes: Flow[HttpRequest, HttpResponse, Any]

    val http = new Service[Any] {
      def bindAndHandle(port: Int)(
          implicit system: ActorSystem,
          mat: Materializer): Task[ServerBinding] =
        IO.fromFuture(_ ⇒ scaladsl.Http().bindAndHandle(routes, "0.0.0.0", port))
    }
  }

}
