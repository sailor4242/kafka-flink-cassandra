package repository

import java.util.UUID

import model._
import doobie._
import doobie.implicits._
//import exceptions.UserNotFound
//import scalaz.OptionT
import scalaz.zio.interop.catz._
import scalaz.zio._
//import cats.implicits._
//import doobie.free.connection
import doobie.h2.implicits._

trait Repository {
  def repository: Repository.Service[Any]
}

object Repository {

  trait Service[R] extends Serializable {

    def getAllStocksByUserId(id: Long): ZIO[R, Throwable, List[Stock]]

    def getAllFundsByUserId(id: Long): ZIO[R, Throwable, List[CurrencyValue]]

    def getUserByUId(uid: UUID): ZIO[R, Throwable, Option[(Long, User)]]

    def createUser(user: User): ZIO[R, Throwable, User]

    def updateUser(user: User): ZIO[R, Throwable, User]

    def updateStockByUserUid(uid: UUID, stock: Stock): ZIO[R, Throwable, Unit]

    def updateFundByUserUid(uid: UUID, fund: CurrencyValue): ZIO[R, Throwable, Unit]

  }

  trait Live extends Repository {

    protected val transactor: Transactor[Task]

    val repository: Repository.Service[Any] = new Repository.Service[Any] {

      override def getUserByUId(uid: UUID): ZIO[Any, Throwable, Option[(Long, User)]] =
        DB
          .getByUID(uid)
          .option
          .transact(transactor)
          .orDie

      override def getAllStocksByUserId(id: Long): ZIO[Any, Nothing, List[Stock]] =
        DB
          .getAllStocksByUserId(id)
          .to[List]
          .transact(transactor)
          .orDie

      override def getAllFundsByUserId(id: Long): ZIO[Any, Nothing, List[CurrencyValue]] =
        DB
          .getAllFundsByUserId(id)
          .to[List]
          .transact(transactor)
          .orDie

      override def createUser(user: User): ZIO[Any, Throwable, User] =
        DB
          .save(user)
          .withUniqueGeneratedKeys[Long]("ID")
          .map(id => user)
          .transact(transactor)
          .orDie

      //
      //          override def update(): ZIO[Any, Nothing, Option[]] =
      //            (for {
      //              oldItem <- DB.get(id).option
      //              newItem = oldItem.map(_.update(todoItemForm))
      //              _ <- newItem.fold(connection.unit)(item => DB.update(item).run.void)
      //            } yield newItem)
      //              .transact(xa)
      //              .orDie
      //
      //        }

      override def updateUser(user: User): ZIO[Any, Throwable, User] = ???


      override def updateStockByUserUid(uid: UUID, stock: Stock): ZIO[Any, Throwable, Unit] = ???

//      override def updateFundByUserUid(uid: UUID, fund: CurrencyValue): Task[Unit] = {
//        val update = for {
//          userWithId <- OptionT(DB.getByUID(uid).option) //.toRight(UserNotFound(uid))
//          up <- OptionT(DB.getFundByUserIdAndCurrency(userWithId._1, fund.currency).option)
//            .map(fundWithId => CurrencyValue(fundWithId._2.value + fund.value, fundWithId._2.currency))
//            .fold(newFund => DB.updateFundByIdAndCurrency(userWithId._1, newFund), DB.updateFundByIdAndCurrency(userWithId._1, fund))
//        } yield up
//        update.run.transact(transactor).void
//      }

      object DB {

        implicit val currencyMeta: Meta[Currency] = Meta[String].timap(Currency.withName)(_.toString)
        implicit val tickerTypeMeta: Meta[TickerType] = Meta[String].timap(TickerType.withName)(_.toString)

        def save(user: User): Update0 =
          sql"""
          INSERT INTO USERS (UID, FIRST_NAME, LAST_NAME)
          VALUES (${user.uid}, ${user.firstName}, ${user.lastName})
          """.update

        def getByUID(uid: UUID): Query0[(Long, User)] = sql"""
          SELECT * FROM USERS WHERE UID = $uid
          """.query[(Long, User)]

        def getAllStocksByUserId(id: Long): Query0[Stock] = sql"""
          SELECT stock, price, currency, quantity, deal_time FROM USER_STOCKS WHERE USER_ID = $id
          """.query[Stock]

        def getAllFundsByUserId(id: Long): Query0[CurrencyValue] = sql"""
            SELECT value, currency FROM USER_FUNDS WHERE USER_ID = $id
            """.query[CurrencyValue]

        def getFundByUserIdAndCurrency(id: Long, currency: Currency): Query0[(Long, CurrencyValue)] = sql"""
            SELECT id, value, currency FROM USER_FUNDS WHERE USER_ID = $id
            AND currency = $currency FOR UPDATE
            """.query[(Long, CurrencyValue)]

        def updateFundByIdAndCurrency(userId: Long, cv: CurrencyValue): Query0[CurrencyValue] = sql"""
            MERGE INTO currency (userId, value, currency) key (userId)
            values ($userId, ${cv.value}, ${cv.currency})
            """.query[CurrencyValue]
      }

    }

  }

}
