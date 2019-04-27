package com.cassquakhttp.models

import com.cassquakhttp.dbconnectors.CassandraDBConn

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
  val getAllByTicker = quote{ticker: String => query[MarketData].filter(_.ticker == ticker) }
  val getAll = quote(query[MarketData])
  def getElementsByTicker = dbStream.run(getAllByTicker(lift("FXUS")))
  def getAllElements = dbStream.run(getAll)
}
