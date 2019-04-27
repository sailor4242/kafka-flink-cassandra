package me.sandbox.sql.streaming.spark.flow



import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
// import me.sandbox.implicits._
import org.apache.spark.sql.types.{StructField, StructType}
// import me.sandbox.sql.streaming.model.{MessageStructType, TickStructType}
// import me.sandbox.sql.streaming.model._
// import me.sandbox.sql.streaming.spark.logic.StructureWindowUdf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
// import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery, Trigger}
// import org.apache.spark.sql.types.{LongType, StringType, TimestampType}
// import org.apache.spark.sql.types.{LongType, StringType}
import org.apache.spark.sql.types.{ StringType}


import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._


import scala.collection.JavaConverters._


object KafkaStructureToConsole {
  val logger = Logger("streaming")

  //  application configuration
  val appConf: Config = ConfigFactory.load
  val debug: Boolean = appConf.getBoolean("debug-mode")
  val banTimeSec: Long = appConf.getDuration("app.ban-time").getSeconds
  val banRecordTTL: Int = (banTimeSec / 1000).toInt
  val cassandraServers: Set[String] = appConf
    .getString("cassandra.server")
    .split(";")
    .map(_.trim)
    .toSet

  val sparkBuilder: SparkSession.Builder = SparkSession
    .builder
    .config("spark.sql.shuffle.partitions", 3)
    .appName(appConf.getString("app.name"))

  val spark: SparkSession =
    if (debug) sparkBuilder.master("local[2]").getOrCreate()
    else sparkBuilder.getOrCreate()


  // kafka streaming
  val dfraw: DataFrame = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", appConf.getString("kafka.brokers"))
    .option("subscribe", appConf.getStringList("kafka.topic").asScala.mkString(","))
    .option("startingOffsets", appConf.getString("kafka.offset.reset"))
    .option("maxOffsetsPerTrigger", appConf.getLong("kafka.max-offset"))
    .load()

  val schema = StructType(
    Seq(
      StructField("tickerType", StringType, nullable = false),
      StructField("time", StringType, nullable = false)
    )
  )

  import spark.implicits._

  val df = dfraw
    .selectExpr("CAST(value AS STRING)").as[String]
    .flatMap(_.split("\n"))

  val jsons = df.select(from_json($"value", schema) as "data").select("data.*")

}