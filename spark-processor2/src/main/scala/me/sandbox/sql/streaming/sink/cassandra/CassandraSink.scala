package me.sandbox.sql.streaming.sink.cassandra

import java.net.InetAddress

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.sql.{DataFrame}
import org.apache.spark.sql.execution.SQLExecution
import org.apache.spark.sql.execution.streaming.Sink
import org.apache.spark.sql.types.StringType

class CassandraSink(server: Set[String],
                    keyspace: String,
                    table: String,
                    columns: Array[String],
                    ttl: Int) extends Sink with Serializable {
  lazy val pool =
    CassandraConnector(
      hosts = server.map(r => InetAddress.getByName(r)))

  override def addBatch(batchId: Long, data: DataFrame): Unit = {
    val queryExecution = data.queryExecution
    val schema = queryExecution.analyzed.output

    SQLExecution.withNewExecutionId(data.sparkSession, queryExecution) {
      queryExecution.toRdd.foreachPartition { rows =>
        pool.withSessionDo { session: Session =>
          for (row <- rows) {
            val objects = new Array[AnyRef](schema.length)
            for ((attr, i) <- schema.zipWithIndex) {
              attr.dataType match {
                case StringType =>
                  objects(i) = row.getString(i)
                case _ =>
                  objects(i) = row.get(i, attr.dataType)
              }
            }
             /*store*/
            session.execute(
              QueryBuilder.insertInto(keyspace, table)
                .values(columns, objects)
                .using(QueryBuilder.ttl(ttl)))
          }
        }
      }
    }
  }
}



object CassandraSink {
  def apply(server: Set[String], keyspace: String, table: String, columns: Array[String], ttl: Int): CassandraSink =
    new CassandraSink(server, keyspace, table, columns, ttl)
}

