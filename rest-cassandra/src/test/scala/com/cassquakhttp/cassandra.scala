package com

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

package object cassquakhttp {

  val r = scala.util.Random

  def await[T](f: Future[T]): T = Await.result(f, Duration.Inf)
}
