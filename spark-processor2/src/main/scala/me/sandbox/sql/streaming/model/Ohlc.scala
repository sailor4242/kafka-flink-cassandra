package me.sandbox.sql.streaming.model

import java.util.Date

case class StreamQueryBar1(
                            time : String,
                            ticker : String,
                            open : Float,
                            high : Float,
                            low : Float,
                            close : Float,
                            vol : Int,
                            curr : String
                          )
case class StreamQueryBar2(time : Date,
                           ticker : String,
                           open : BigDecimal,
                           high : BigDecimal,
                           low : BigDecimal,
                           close : BigDecimal,
                           vol : Int,
                           curr : String
                          )

case class StreamJson(tickerName: String, time: String, value: Double, curr: String,  volume: Int)
