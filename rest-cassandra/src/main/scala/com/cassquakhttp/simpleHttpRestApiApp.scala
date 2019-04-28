package com.cassquakhttp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.cassquakhttp.http.marketDataHttpRoute

object simpleHttpRestApiApp extends App  {
  println("Starting")
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  val port = 8076
  Http().bindAndHandle(marketDataHttpRoute.route, "localhost", port)
  println(s"akka http server is started on port : $port ")
}
