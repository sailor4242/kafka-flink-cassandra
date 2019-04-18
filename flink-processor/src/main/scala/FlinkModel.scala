import java.util.UUID

case class OHLC1Min(
  id: UUID,
  ticker: String,
  tStart: String,
  open: String,
  high: String,
  low: String,
  close: String,
  vol: Int,
  currency: String) {
  override def toString: String = s"OHLC1m[$ticker , $tStart, $open, $high $low $close $vol]"
}

final case class TickerCassandra(
  id: UUID,
  ticker: String,
  time: String,
  price: String,
  volume: Int,
  currency: String) {
  override def toString: String = s"$ticker $time $price $volume $currency"
}

