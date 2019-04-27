package me.sandbox.sql.streaming.sink.cassandra

import org.apache.spark.sql.Row



object CassandraTableWriterModels {


  object  OhlcWriter extends  CassandraTableWriter {

    override def putQuery(row: Row , keySpace : String, tableName: String): String = {
      s"""insert into $keySpace.$tableName ( some another logic here )
           values('${row(0)}', ${row(1)}, ${row(2)}, '${row(3)}', ${row(4)})"""
    }
  }

  object OhlcTimeWindowsWriter extends CassandraTableWriter {
    override def putQuery(row: Row, keySpace : String , tableName: String ): String = {
      s"""insert into $keySpace.$tableName
         |(bar_time, ticker, open, high, low , close , volume , currency)
         | VALUES('${row.getAs[String]("bar_time")}',
         |   '${row.getAs[String]("ticker")}',
         |    ${row.getAs[Float]("open")},
         |    ${row.getAs[Float]("high")},
         |    ${row.getAs[Float]("low")},
         |    ${row.getAs[Float]("close")},
         |    ${row.getAs[Int]("vol")},
         |   '${row.getAs[String]("curr")}' );""".stripMargin
    }
  }
}



