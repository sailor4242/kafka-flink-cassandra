package com.cassquakhttp.http

import akka.http.scaladsl.model.StatusCodes
import akka.stream.ThrottleMode
import akka.stream.scaladsl.{Flow, Source}
import com.cassquakhttp.db.connections.CassandraDBConn
import com.cassquakhttp.models.MarketData
import com.cassquakhttp.protocols.json.JsonImplicits

import scala.concurrent.duration._

object marketDataHttpRoute extends JsonImplicits with CassandraDBConn {

  private def getThrottlingFlow[T] = Flow[T]
    .throttle(elements = 1, per = 10.millis, maximumBurst = 0, mode = ThrottleMode.shaping)

  import akka.http.scaladsl.server.Directives._

  def route = {
    pathPrefix("data") {
      (get & parameter("ticker") & parameter("vol".as[Int])) { (ticker, vol) =>
        (ticker, vol) match {
          case (t, v) =>
            //println(s" ticker = $t and volume = $v ")
            complete(Source.fromPublisher(MarketData
              .getAllElements
              .filter(_.ticker == t).filter(_.volume > v.toInt).toReactivePublisher)
              .via(getThrottlingFlow[MarketData]))

          case _ => complete(StatusCodes.BadRequest)
        }
      }
    } ~
      path("allData") {
        complete(Source.fromPublisher(MarketData
          .getAllElements
          .toReactivePublisher)
          .via(getThrottlingFlow[MarketData])
        )
      }
  }

}
