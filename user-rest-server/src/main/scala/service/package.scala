import java.util.UUID

import scalaz.zio.ZIO
import model._
import service._

package object userService extends UserService.Service[UserService] {

  override def create(user: User): ZIO[UserService, Throwable, UserAccount] =
    ZIO.accessM(_.userService create user)

  override def getById(id: UUID): ZIO[UserService, Throwable, UserAccount] =
    ZIO.accessM(_.userService getById id)

  override def buyStock(uid: UUID, stock: StockOp): ZIO[UserService, Throwable, UserAccount] =
    ZIO.accessM(_.userService buyStock(uid, stock))

  override def sellStock(uid: UUID, stock: StockOp): ZIO[UserService, Throwable, UserAccount] =
    ZIO.accessM(_.userService sellStock(uid, stock))

  override def addFund(uid: UUID, fund: CurrencyValue): ZIO[UserService, Throwable, UserAccount] =
    ZIO.accessM(_.userService addFund(uid, fund))
}
