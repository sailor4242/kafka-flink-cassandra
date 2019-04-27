package me.sandbox

import java.sql.Timestamp

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

package object implicits {
  implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration =
    scala.concurrent.duration.Duration.fromNanos(d.toNanos)
  implicit class TimestampOps(val left: Timestamp) extends AnyVal {
    def min(right: Timestamp): Timestamp = {
      if (left != null && right != null) {
        if (left.before(right)) left else right
      } else {
        Option(left).getOrElse(right)
      }
    }

    def max(right: Timestamp): Timestamp = {
      if (left != null && right != null) {
        if (right.after(left)) right else left
      } else {
        Option(left).getOrElse(right)
      }
    }
  }

}
