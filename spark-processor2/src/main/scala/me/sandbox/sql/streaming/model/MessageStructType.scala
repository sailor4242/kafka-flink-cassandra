package me.sandbox.sql.streaming.model

import org.apache.spark.sql.types._
// import org.apache.


object MessageStructType {
  val schema: StructType = new StructType()
    .add("type", StringType, nullable = false)
    .add("ip", StringType, nullable = false)
    .add("unix_time", LongType, nullable = false)
    .add("url", StringType, nullable = false)
}

object TickStructType {
  val schema = StructType {
    Seq(
      StructField("tickerType", StringType, nullable = false),
      StructField("time", StringType, nullable = false),
      StructField("last", StructType  {
        Seq(
          StructField("value", DoubleType, nullable = false) ,
          StructField("currency", StringType, nullable = false)
        )
      }, nullable = false
      ),
      StructField("volume", IntegerType, nullable = false)
    )
  }
}
