package com.cassquakhttp.models

import com.cassquakhttp.db.connections.CassandraDBConn

final case class MarketData(
                             barTime: String,
                             ticker: String,
                             close: Float,
                             currency: String,
                             high: Float,
                             low: Float,
                             open: Float,
                             volume: Int
                           )
object MarketData extends CassandraDBConn {
  import dbStream._
  val getAllByTicker = quote{tickerQuery: String => query[MarketData].filter(t => t.ticker ==  tickerQuery) }
  val getElFilterByVolume = quote{vol: Int => query[MarketData].filter(_.volume > vol) }

  val getAll = quote(query[MarketData])



  def getAllElements = dbStream.run(getAll)
}
