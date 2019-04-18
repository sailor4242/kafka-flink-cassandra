import java.util.UUID

import model.Ticker
import org.apache.flink.streaming.api.scala.function._
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

object FlinkWindowProcessFunc extends ProcessAllWindowFunction[Ticker, OHLC1Min, TimeWindow] {
  override def process(
    context: FlinkWindowProcessFunc.Context,
    xs: Iterable[Ticker], out: Collector[OHLC1Min]): Unit = {
    val wtStart = xs.map(x => x.time).head
    // val wtEnd = xs.map(x => x.time).last
    val open = xs.map(x => x.last.value).head
    val high = xs.map(x => x.last.value).max
    val low = xs.map(x => x.last.value).min
    val close = xs.map(x => x.last.value).last
    val aggVolume = xs.map(x => x.volume).sum
    out.collect(OHLC1Min(
      UUID.randomUUID(),
      xs.head.tickerType.toString,
      wtStart,
      open.toString,
      high.toString,
      low.toString,
      close.toString,
      aggVolume,
      xs.head.last.currency.toString))

  }
}

