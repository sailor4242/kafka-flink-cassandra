package me.sandbox.sql.streaming.source.rate

// import me.sandbox.sql.streaming.source.StreamingSource
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, SparkSession}

trait RateSource  {

  val spark: SparkSession
  val rowsPerSecond: String
  val numPartitions: String

   def readStream(): DataFrame = {
    spark.readStream
      .format("rate")
      .option("rowsPerSecond", rowsPerSecond)
      .option("numPartitions", numPartitions)
      .load()
      .select(col("*"))
  }

}
