package com.cassquakhttp.db.connections

import io.getquill.{CassandraAsyncContext, CassandraStreamContext, SnakeCase}
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService

trait CassandraDBConn {
  implicit val scheduler: SchedulerService = Scheduler.io()
  val db = new CassandraAsyncContext(SnakeCase, "db")
  val dbStream = new CassandraStreamContext(SnakeCase, "db")

}
