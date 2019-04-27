package me.sandbox.sql.streaming.sink.cassandra

import org.apache.spark.sql.Row

trait CassandraTableWriter extends Serializable {
  def putQuery(row : Row , keySpace : String, tableName: String ): String

}
