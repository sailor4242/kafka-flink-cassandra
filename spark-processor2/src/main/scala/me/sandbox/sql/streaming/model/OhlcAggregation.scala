package me.sandbox.sql.streaming.model

import java.util.Date

import cats.kernel.Semigroup


case class OhlcAggregation(barStart: Date,
                           barEnd: Date,
                           ticker: String,
                           open: BigDecimal,
                           high: BigDecimal,
                           low: BigDecimal,
                           close: BigDecimal,
                           vol: Int,
                           curr: String
                          )

object OhlcAggregation {
  implicit val group: Semigroup[OhlcAggregation] = new Semigroup[OhlcAggregation] {
    override def combine(x: OhlcAggregation, y: OhlcAggregation): OhlcAggregation = {

      OhlcAggregation(
        (x.barStart :: y.barStart :: Nil).minBy(_.getTime),
        (x.barEnd :: y.barEnd :: Nil).maxBy(_.getTime),
        x.ticker,
        (x :: y :: Nil).minBy(_.barStart.getTime).open,
        x.high max y.high,
        x.low min y.low,
        (x :: y :: Nil).maxBy(_.barEnd.getTime).close,
        x.vol + y.vol,
        x.curr
      )
    }
  }
}