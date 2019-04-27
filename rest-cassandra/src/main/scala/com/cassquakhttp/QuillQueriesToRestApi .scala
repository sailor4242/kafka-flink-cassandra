package com.cassquakhttp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{ActorMaterializer, ThrottleMode}
import com.cassquakhttp.dbconnectors.CassandraDBConn
import com.cassquakhttp.models.MarketData
import spray.json._

// todo вынести это в папучку protocols
trait Protocols extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val TableOhlc30sFormat = jsonFormat8(MarketData.apply)
}

import scala.concurrent.duration._

// todo поменять на HttpApp потом
object QuillSimpleQueries2 extends App with Protocols with CassandraDBConn {
  println("Starting")
  // todo куда засунуть имплициды эти посмотри на микросервисы Олега
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val jsonStreamingSupport = EntityStreamingSupport.json()
    .withParallelMarshalling(parallelism = 2, unordered = true)

  def getThrottlingFlow[T] = Flow[T]
    .throttle(elements = 1, per = 10.millis, maximumBurst = 0, mode = ThrottleMode.shaping)

  val streamSource = Source.fromPublisher(MarketData.getAllElements.toReactivePublisher)

  def route =
    path("hi") {
      get {
        complete(streamSource.via(getThrottlingFlow[MarketData]))
      }
    }

  val port = 8077
  Http().bindAndHandle(route, "localhost", port)
  println(s"akka http server is started on port : $port ")
}