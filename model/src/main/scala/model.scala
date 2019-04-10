import enumeratum._
import io.circe.generic.JsonCodec

import scala.collection.immutable

@JsonCodec
final case class Ticker(tickerType: TickerType,
                        time: String,
                        last: Last,
                        volume: Int)

@JsonCodec
final case class Last(value: BigDecimal, currency: Currency)

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