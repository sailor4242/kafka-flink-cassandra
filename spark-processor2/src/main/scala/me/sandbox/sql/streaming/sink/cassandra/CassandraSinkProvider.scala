package me.sandbox.sql.streaming.sink.cassandra

import cats.data.Validated.{Invalid, Valid}
import cats.data._
import cats.syntax.apply._
import cats.syntax.option._
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.execution.streaming.Sink
import org.apache.spark.sql.sources.StreamSinkProvider
import org.apache.spark.sql.streaming.OutputMode

import scala.util.Try

class CassandraSinkProvider extends StreamSinkProvider {
  type ValidationResult[A] = ValidatedNel[String, A]

  override def createSink(
    sqlContext: SQLContext,
    parameters: Map[String, String],
    partitionColumns: Seq[String],
    outputMode: OutputMode): Sink = {

    val server: ValidationResult[Set[String]] = parameters.get("cassandra.server").map(_.split(',').map(_.trim).toSet)
      .toValidNel("cassandra.server is used to find servers")

    val keyspace: ValidationResult[String] = parameters.get("cassandra.keyspace")
      .toValidNel("cassandra.keyspace is used to select a keyspace")

    val table: ValidationResult[String] = parameters.get("cassandra.table")
      .toValidNel("cassandra.table is used to select a table")

    val columns: ValidationResult[Array[String]] = parameters.get("cassandra.columns").map(_.split(',').map(_.trim))
      .toValidNel("cassandra.columns should define cassandra.table columns")

    val ttl: ValidationResult[Int] = Try(parameters.get("cassandra.ttl").map(_.toInt).getOrElse(0)).toOption
      .toValidNel("cassandra.ttl is empty or has invalid value")

    (server, keyspace, table, columns, ttl).mapN(CassandraSink.apply) match {
      case Valid(sink) => sink
      case Invalid(errors) =>
        throw new IllegalArgumentException("Incorrect sink configuration:\n" + errors.toList.mkString("\n"))
    }
  }
}
