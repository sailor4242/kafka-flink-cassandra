package service

import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import repository.Repository
import scalaz.zio.ZIO
import model._

trait UserService {
  def userService: UserService.Service[Any]
}

object UserService {

  trait Service[R] {

    def create(user: User): ZIO[R, Throwable, UserAccount]

    def getById(uid: UUID): ZIO[R, Throwable, UserAccount]

    def buyStock(uid: UUID, stock: StockOp): ZIO[R, Throwable, UserAccount]

    def sellStock(uid: UUID, stock: StockOp): ZIO[R, Throwable, UserAccount]

    def addFund(uid: UUID, fund: CurrencyValue): ZIO[R, Throwable, UserAccount]

  }

  trait Live extends UserService with StrictLogging {

    protected val repository: Repository.Service[Any]

    def userService: UserService.Service[Any] = new UserService.Service[Any] {

      override def create(user: User): ZIO[Any, Throwable, UserAccount] = for {
        u <- repository createUser user
      } yield UserAccount(u)

      override def getById(uid: UUID): ZIO[Any, Throwable, UserAccount] = (for {
        op <- repository getUserByUId uid
        p <- ZIO.fromOption(op)
        stocks <- repository getAllStocksByUserId p._1
        funds <- repository getAllFundsByUserId p._1
      } yield UserAccount(p._2, stocks, funds)).orDieWith(ex => new NullPointerException("as"))

      override def buyStock(uid: UUID, stock: StockOp): ZIO[Any, Throwable, UserAccount] = ???

      override def sellStock(uid: UUID, stock: StockOp): ZIO[Any, Throwable, UserAccount] = ???

      override def addFund(uid: UUID, fund: CurrencyValue): ZIO[Any, Throwable, UserAccount] = ???
    }
  }

}