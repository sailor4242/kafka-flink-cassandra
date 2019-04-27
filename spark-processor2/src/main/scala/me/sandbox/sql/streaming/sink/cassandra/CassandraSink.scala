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
            /* store */
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


/*def process(record: org.apache.spark.sql.Row) = {
  println(s"Process new $record")
  if (cassandraDriver == null) {
    cassandraDriver = new CassandraDriver()
  }
  cassandraDriver.connector.withSessionDo(session =>

      session.execute(
      s"""insert into ${cassandraDriver.namespace}.${cassandraDriver.foreachTableSink}
         |(bar_time, ticker, open, high, low , close , volume , currency)
         | VALUES('${record(0)}','${record(1)}', '${record(2)}',  '${record(3)}','${record  (4)}',
         |'${record(5)}', ${record(6)}, '${record(7)}' );""".stripMargin
    )
  )
}*/

/*def process(record: Row) = {
 println(s"Process new $record")
  val test1 = record.getAs[Double]("open") // open price of the bar
  val test2 = record.getAs[Double]("high")

  println(s" Test ${test1 + test2}")
  println(s" Test1 ${test1}")
  println(s" Test2 ${test2}")
 if (cassandraDriver == null) {
   cassandraDriver = new CassandraDriver()
 }
 cassandraDriver.connector.withSessionDo(session =>
   session.execute(
     s"""
    insert into ${cassandraDriver.namespace}.${cassandraDriver.foreachTableSink}
    (bar_time,  ticker, open, high, low , close , volume , currency  )
    VALUES(
    '${record.getAs[String]("bar_time")}', // bar_time
    '${record.getAs[String]("ticker")}', // ticker ( name of stock on a particular market )
    '${record.getAs[Double]("open")}', // open price of the bar
    '${record.getAs[Double]("high")}', // high price of the bar
    //
    '${record.getAs[Double]("low")}', // low ...
    '${record.getAs[Double]("close")}', // close ...
    '${record.getAs[Int]("vol")}', // volume ...
    '${record.getAs[String]("curr")}' // currency ...
     )""")

 )
}*/



/*def process(record: Row) = {
  println(s"Process new $record")
   val test1 = record.getAs[Double]("open") // open price of the bar
   val test2 = record.getAs[Double]("high")

   println(s" Test ${test1 + test2}")
   println(s" Test1 ${test1}")
   println(s" Test2 ${test2}")
  if (cassandraDriver == null) {
    cassandraDriver = new CassandraDriver()
  }
  cassandraDriver.connector.withSessionDo(session =>
    session.execute(
      s"""
     insert into ${cassandraDriver.namespace}.${cassandraDriver.foreachTableSink}
     (bar_time,  ticker, open, high, low , close , volume , currency  )
     VALUES( ? , ? , ? , ? , ? , ? , ? , ? ) """ ,
     record.getAs[String]("bar_time"), // bar_time
     record.getAs[String]("ticker"), // ticker ( name of stock on a particular market )
      record.getAs[Double]("open"), // open price of the bar
     record.getAs[Double]("high"), // high price of the bar
     //
     record.getAs[Double]("low"), // low ...
     record.getAs[Double]("close"), // close ...
     record.getAs[Int]("vol"), // volume ...
     record.getAs[String]("curr") // currency ...
      )

  )
}*/


/*  def process(record: Row) = {
    println(s"Process new $record")
    if (cassandraDriver == null) {
      cassandraDriver = new CassandraDriver()
    }
    cassandraDriver.connector.withSessionDo(session => {
      val bound = session.prepare(
        s"""
            INSERT INTO ${cassandraDriver.namespace}.${cassandraDriver.foreachTableSink}
            (bar_time,  ticker, open, high, low , close , volume , currency  )
            VALUES( ? , ? , ? , ? , ? , ? , ? , ? )"""
      ).bind(
        record(0).asInstanceOf[Object], // bar_time
        record(1).asInstanceOf[Object], // ticker ( name of stock on a particular market )
        record(2).asInstanceOf[Object], // open price of the bar
        record(3).asInstanceOf[Object], // high price of the bar
        record(4).asInstanceOf[Object], // low ...
        record(5).asInstanceOf[Object],    // close
        record(6).asInstanceOf[Object],       // volume
        record(7).asInstanceOf[Object]         // currency
      )
      session.execute(bound)
    } )
  }*/

/* ).bind(
   record(0), // bar_time
   record(1).asInstanceOf[AnyRef], // ticker ( name of stock on a particular market )
   record.getDouble(2).asInstanceOf[AnyRef], // open price of the bar
   record.getDouble(3).asInstanceOf[AnyRef], // high price of the bar
   record.getDouble(4).asInstanceOf[AnyRef], // low ...
   record.getDouble(5).asInstanceOf[AnyRef],    // close
   record.asInstanceOf[AnyRef],       // volume
   record(7).asInstanceOf[AnyRef]         // currency
 )
 session.execute(bound)
} )*/