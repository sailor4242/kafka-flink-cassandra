package me.sandbox.sql.streaming.sink.cassandra

import java.net.InetAddress

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.sql.{ForeachWriter, Row, SparkSession}



case class CassandraSinkForeach(sparkSession : SparkSession,
                           nameSpace : String,
                           tableName: String,
                           writer : CassandraTableWriter)  extends  ForeachWriter[Row] {

  val connector = CassandraConnector(sparkSession.sparkContext.getConf)

  def open(partitionId: Long, version: Long): Boolean = true // open connection

  def process(record: Row): Unit = {
    println(s"Process new $record")
    connector.withSessionDo{session =>
      session.execute( writer.putQuery(record,nameSpace, tableName ))
    }
    () // suppress Error:(23, 28) discarded non-Unit value
  } // todo find annotation
  def close(errorOrNull: Throwable): Unit = {  /* close the connection */ }
}


class CassandraSinkForeachDataStax(servers: Set[String],
                           keyspace: String,
                           table: String,
                           columns: Array[String],
                           ttl: Int) extends ForeachWriter[Row] {

  /* Cassandra connector */
  val pool =  CassandraConnector(  hosts = servers.map(r => InetAddress.getByName(r.trim)))

  /* we support duplicates and don't have to check is a partition was saved */
  override def open(partitionId: Long, version: Long): Boolean = true

  override def process(row: Row): Unit = {
    pool.withSessionDo { session: Session =>
      val objects = row.toSeq.asInstanceOf[Seq[AnyRef]]

      session.execute( QueryBuilder.insertInto(keyspace, table)
          .values(columns, objects.toArray)
          .using(QueryBuilder.ttl(ttl)))
      () // Suppress “discarded non-Unit value” warning
    }
  }
  // todo what is the question below
  /* how we can close it manually ? */
  override def close(errorOrNull: Throwable): Unit = {}
}