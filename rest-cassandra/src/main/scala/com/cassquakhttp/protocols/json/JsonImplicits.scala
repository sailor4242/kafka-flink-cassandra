package com.cassquakhttp.protocols.json

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.cassquakhttp.models.MarketData
import spray.json.DefaultJsonProtocol

trait JsonImplicits extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val TableOhlc30sFormat = jsonFormat8(MarketData.apply)
  implicit val jsonStreamingSupport = EntityStreamingSupport.json()
    .withParallelMarshalling(parallelism = 2, unordered = true)
}