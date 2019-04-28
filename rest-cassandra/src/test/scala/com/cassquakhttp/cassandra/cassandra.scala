package com.cassquakhttp

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

package object cassandra {

  val r = scala.util.Random

  def await[T](f: Future[T]): T = Await.result(f, Duration.Inf)
}
