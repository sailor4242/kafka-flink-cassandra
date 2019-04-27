package me.sandbox.sql.streaming.model

object EventType extends Enumeration {
  type EventType = Value

  val buyLimitOrder: EventType = Value("buy_limit_order")
  val buyMarketOrder: EventType = Value("buy_market_order")
  val sellLimitOrder: EventType = Value("sell_limit_order")
  val sellMarketOrder: EventType = Value("sellMarketOrder")
  val placeStopOrder: EventType = Value("place_stop_order")
  // ...
}
