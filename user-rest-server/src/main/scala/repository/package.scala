import java.util.UUID

import scalaz.zio._
import model._

package object repository extends Repository.Service[Repository] {

  override def getAllStocksByUserId(id: FiberId): ZIO[Repository, Throwable, List[Stock]] =
    ZIO.accessM(_.repository getAllStocksByUserId id)

  override def getAllFundsByUserId(id: FiberId): ZIO[Repository, Throwable, List[CurrencyValue]] =
    ZIO.accessM(_.repository getAllFundsByUserId id)

  override def getUserByUId(uid: UUID): ZIO[Repository, Throwable, Option[(Long, User)]] =
    ZIO.accessM(_.repository getUserByUId uid)

  override def createUser(user: User): ZIO[Repository, Throwable, User] =
    ZIO.accessM(_.repository createUser user)

  override def updateUser(user: User): ZIO[Repository, Throwable, User] =
    ZIO.accessM(_.repository updateUser user)

  override def updateStockById(id: FiberId, stock: Stock): ZIO[Repository, Throwable, Unit] =
    ZIO.accessM(_.repository updateStockById (id, stock))

  override def updateFundById(id: FiberId, fund: CurrencyValue): ZIO[Repository, Throwable, Unit] =
    ZIO.accessM(_.repository updateFundById (id, fund))
}
