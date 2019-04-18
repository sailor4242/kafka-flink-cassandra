import java.util.UUID

import enumeratum._
import io.circe.generic.JsonCodec

import scala.collection.immutable

package model {

  @JsonCodec
  final case class Ticker(
    tickerType: TickerType,
    time: String,
    last: CurrencyValue,
    volume: Int)

  @JsonCodec
  final case class CurrencyValue(
    value: BigDecimal,
    currency: Currency)

  sealed trait TickerType extends EnumEntry

  object TickerType extends Enum[TickerType] with CirceEnum[TickerType] {

    case object FXUS extends TickerType
    case object FXDE extends TickerType
    case object FXRU extends TickerType

    def values: immutable.IndexedSeq[TickerType] = findValues
  }

  sealed trait Currency extends EnumEntry

  object Currency extends Enum[Currency] with CirceEnum[Currency] {

    case object RUB extends Currency
    case object USD extends Currency
    case object EUR extends Currency

    def values: immutable.IndexedSeq[Currency] = findValues
  }

  @JsonCodec
  final case class CreateUser(firstName: String, lastName: String) {
    def asUser: User = User(UUID.randomUUID(), firstName, lastName)
    def asUser(uid: String): User = User(UUID.fromString(uid), firstName, lastName)
  }

  @JsonCodec
  final case class User(
    uid: UUID,
    firstName: String,
    lastName: String)

  @JsonCodec
  final case class UserAccount(
    user: User,
    stocks: List[Stock] = List(),
    funds: List[CurrencyValue] = List(),
    revenue: CurrencyValue = CurrencyValue(0, Currency.USD))

  @JsonCodec
  final case class Stock(
    tickerType: TickerType,
    time: String,
    price: CurrencyValue,
    volume: Int)

  @JsonCodec
  final case class StockOp(
    tickerType: TickerType,
    volume: Int)

}