
import akka.http.scaladsl.Http.ServerBinding
import akka.actor.ActorSystem
import akka.stream.Materializer
import scalaz.zio._

package object http extends Http.Service[Http] {

  def bindAndHandle(port: Int)(
    implicit system: ActorSystem,
    mat: Materializer): ZIO[Http, Throwable, ServerBinding] =
    ZIO.accessM(_.http bindAndHandle port)

}
