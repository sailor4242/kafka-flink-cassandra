package me.sandbox.sql.streaming.source.kafka

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import me.sandbox.sql.streaming.model.MessageStructType
import me.sandbox.sql.streaming.source.StreamingSource
import org.apache.spark.sql.functions.{col, from_json}
import org.apache.spark.sql.types.{StringType, StructType, TimestampType}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Structure streaming variant of the same task.
  */
object KafkaToSparkModels {

  case class KafkaToSpark(sparkSession: SparkSession) extends StreamingSource {

    import sparkSession.implicits._

    val kafkaConfig: Config = ConfigFactory.load().getConfig("kafka")

    override def readStream: DataFrame =
      sparkSession.readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", kafkaConfig.getString("BOOTSTRAP_SERVERS"))
        // .option("startingOffsets", appConf.getString("kafka.offset.reset"))
        // .option("maxOffsetsPerTrigger", appConf.getLong("kafka.max-offset"))
        .option("subscribe", kafkaConfig.getString("TOPIC"))
        .load()


    def readJsonStream(schema: StructType): DataFrame =
      readStream.selectExpr("CAST(value AS STRING)")
        .select(from_json($"value", schema) as "data")
        .select("data.*")


  }


  object KafkaToSparkRC2 {
    val logger = Logger("streaming")

    /* application configuration */
    val appConf = ConfigFactory.load
    val debug = appConf.getBoolean("debug-mode")
    val banTimeSec = appConf.getDuration("app.ban-time").getSeconds
    val banRecordTTL = (banTimeSec / 1000).toInt
    val cassandraServers = appConf.getString("cassandra.server").split(";").map(_.trim).toSet

    val sparkBuilder = SparkSession
      .builder
      .config("spark.sql.shuffle.partitions", 3)
      .appName(appConf.getString("app.name"))

    val spark =
      if (debug) sparkBuilder.master("local[2]").getOrCreate()
      else sparkBuilder.getOrCreate()


    /* kafka streaming */
    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", appConf.getString("kafka.brokers"))
      //.option("subscribe", appConf.getStringList("kafka.topic").asScala.mkString(","))

      .load()

    /* key = user_ip, value =  */
    val parsed =
      df
        .select(
          col("key").cast(StringType),
          from_json(col("value").cast(StringType), schema = MessageStructType.schema).alias("value"))
        .withColumn("eventTime", col("value.unix_time").cast(TimestampType))
        .selectExpr("key as ip", "value.type as action", "eventTime")

  }

}

