package repository

import java.util.UUID

import model._
import doobie._
import doobie.implicits._
import scalaz.zio.interop.catz._
import scalaz.zio._
import cats.implicits._
import doobie.free.connection
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

    def updateStockById(id: Long, stock: Stock): ZIO[R, Throwable, Unit]

    def updateFundById(id: Long, fund: CurrencyValue): ZIO[R, Throwable, Unit]

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

      //          override def getAllStocksByUserId(id: Long): ZIO[Any, Nothing, List[Stock]] = ???
      //            DB
      //              .getAllStocksByUserId
      //              .to[List]
      //              .transact(xa)
      //              .orDie

      //          override def getAllFundsByUserId(id: Long): ZIO[Any, Nothing, List[CurrencyValue]] = ???
      //            DB
      //              .getAllFundsByUserId
      //              .to[List]
      //              .transact(xa)
      //              .orDie


      //          override def delete(id: TodoId): ZIO[Any, Nothing, Unit] = ???
      //          DB
      //            .delete(id)
      //            .run
      //            .transact(xa)
      //            .void
      //            .orDie
      //
      //          override def deleteAll: ZIO[Any, Nothing, Unit] =
      //            DB
      //              .deleteAll
      //              .run
      //              .transact(xa)
      //              .void
      //              .orDie

      override def createUser(user: User): ZIO[Any, Throwable, User] =
        DB
          .save(user)
          .withUniqueGeneratedKeys[Long]("ID")
          .map(id => user)
          .transact(transactor)
          .orDie

      //
      //          override def update(): ZIO[Any, Nothing, Option[TodoItem]] =
      //            (for {
      //              oldItem <- DB.get(id).option
      //              newItem = oldItem.map(_.update(todoItemForm))
      //              _ <- newItem.fold(connection.unit)(item => DB.update(item).run.void)
      //            } yield newItem)
      //              .transact(xa)
      //              .orDie
      //
      //        }
      override def getAllStocksByUserId(id: FiberId): ZIO[Any, Throwable, List[Stock]] = ???

      override def getAllFundsByUserId(id: FiberId): ZIO[Any, Throwable, List[CurrencyValue]] = ???

      override def updateUser(user: User): ZIO[Any, Throwable, User] = ???

      override def updateStockById(id: FiberId, stock: Stock): ZIO[Any, Throwable, Unit] = ???

      override def updateFundById(id: FiberId, fund: CurrencyValue): ZIO[Any, Throwable, Unit] = ???
    }

    object DB {

      def save(user: User): Update0 = sql"""
          INSERT INTO USERS (UID, FIRST_NAME, LAST_NAME)
          VALUES (${user.uid}, ${user.firstName}, ${user.lastName})
          """.update

      def getByUID(uid: UUID): Query0[(Long, User)] = sql"""
          SELECT * FROM USERS WHERE UID = $uid
          """.query[(Long, User)]

      //
      //    def getAllStocksByUserId(id: Long): Query0[Stock] = sql"""
      //      SELECT * FROM USERS WHERE USER_ID = $id
      //      """.query[Stock]
      //
      //    def getAllStocksByUserId(id: Long): Query0[Fund] = sql"""
      //      SELECT * FROM USER_FUNDS WHERE USER_ID = $id
      //      """.query[Fund]
      //
      //    def delete(id: TodoId): Update0 =
      //      sql"""
      //      DELETE from TODOS WHERE ID = ${id.value}
      //      """.update
      //
      //    val deleteAll: Update0 =
      //      sql"""
      //      DELETE from TODOS
      //      """.update
      //
      //    def update(todoItem: TodoItem): Update0 =
      //      sql"""
      //        UPDATE TODOS SET
      //        TITLE = ${todoItem.item.title},
      //        COMPLETED = ${todoItem.item.completed},
      //        ORDERING = ${todoItem.item.order}
      //        WHERE ID = ${todoItem.id.value}
      //      """.update
    }

  }

}
