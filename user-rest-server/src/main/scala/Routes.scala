import java.util.UUID

import akka.http.scaladsl.server.{ Directives, Route }
import scalaz.Monoid
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport.marshaller
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport.unmarshaller
import akka.http.scaladsl.marshalling.GenericMarshallers.futureMarshaller
import akka.http.scaladsl.marshalling.Marshaller
import io.circe.generic.auto._
import scalaz.zio._
import Directives._
import UserRestServer.unsafeRunToFuture
import model._
import service.UserService

import scala.concurrent.Future

object Routes {

  implicit val routeMonoid: Monoid[Task[Route]] = new Monoid[Task[Route]] {
    override def zero: Task[Route] = Task.apply(reject)
    override def append(f1: Task[Route], f2: => Task[Route]): Task[Route] = f1.zipWith(f2)(_ ~ _)
  }
}

case class Routes(userService: UserService.Service[Any]) {

  implicit def zioMarshaller[A, B](implicit futureMarshaller: Marshaller[Future[A], B]): Marshaller[ZIO[UserService.Service[Any], Throwable, A], B] =
    futureMarshaller.compose(io ⇒ unsafeRunToFuture(io.provide(userService)))

  def routes: Route = {
    pathPrefix("account") {
      createUser ~
        pathPrefix(JavaUUID) { uid: UUID ⇒
          getUser(uid) ~ buyStock(uid) ~ sellStock(uid) ~ addFund(uid)
        }
    }
  }

  def getUser(uid: UUID) = get {
    complete(userService.getById(uid))
  }

  def createUser = post {
    entity(as[CreateUser]) { user ⇒
      complete(userService.create(user.asUser))
    }
  }

  def sellStock(uid: UUID) = post {
    path("sell") {
      entity(as[StockOp]) { stock ⇒
        complete(userService.sellStock(uid, stock))
      }
    }
  }

  def buyStock(uid: UUID) = post {
    path("buy") {
      entity(as[StockOp]) { stock ⇒
        complete(userService.buyStock(uid, stock))
      }
    }
  }

  def addFund(uid: UUID) = post {
    path("add") {
      entity(as[CurrencyValue]) { curValue ⇒
        complete(userService.addFund(uid, curValue))
      }
    }
  }

}
