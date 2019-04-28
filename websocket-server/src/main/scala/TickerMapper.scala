import akka.util.ByteString
import model._

trait TickerMapper {
  def map(line: ByteString): Ticker
}

class ETFTickerMapper extends TickerMapper {

  val inputDateTimeFormat = new java.text.SimpleDateFormat("yyyyMMddhhmmss")
  val outputDateTimeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

  override def map(line: ByteString): Ticker = {
    val str = line.utf8String.split(",").map(_.trim)
    Ticker(
      TickerType.withName(str(0)),
      outputDateTimeFormat.format(inputDateTimeFormat.parse(str(1) + str(2))),
      CurrencyValue(BigDecimal(str(3)), Currency.RUB),
      str(4).toInt)
  }
}

object ETFTickerMapper {
  def apply: ETFTickerMapper = new ETFTickerMapper()
}
