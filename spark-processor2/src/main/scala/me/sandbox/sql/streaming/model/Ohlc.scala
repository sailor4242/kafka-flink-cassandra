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


/*
case class Message(eventType: EventType, ip: String, time: Long, url: String)

object Message {
  // todo вот это надо удалить и взять за основу как образец -> копипаснуть в
  //  todo закомментированном виде к своей модели
  //noinspection ConvertExpressionToSAM
  implicit val decodeEvent: Decoder[Message] = new Decoder[Message] {
    override def apply(c: HCursor): Result[Message] = {
      for {
        eventType <- c.downField("type").as[String]
        userIp <- c.downField("ip").as[String]
        requestTime <- c.downField("unix_time").as[Long]
        url <- c.downField("url").as[String]
      } yield {
        new Message(EventType.values.find(_.toString == eventType).getOrElse(EventType.Unknown), userIp, requestTime, url)
      }
    }
  }
}
*/
