package me.sandbox

import com.typesafe.config.{Config, ConfigFactory}
import me.sandbox.sql.streaming.model.TickStructType
import me.sandbox.sql.streaming.sink.cassandra.{CassandraSinkForeach, CassandraTableWriterModels}
import me.sandbox.sql.streaming.source.kafka.KafkaToSparkModels
import me.sandbox.sql.streaming.spark.logic.StructureWindowUdf
import org.apache.spark.SparkConf
import org.apache.spark.sql.streaming.StreamingQuery
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkStructStreaming {
  System.setProperty("hadoop.home.dir", "C:/Hadoop")

  val sparkConfig: Config = ConfigFactory.load().getConfig("spark")
  val cassandraConf : Config =  ConfigFactory.load().getConfig("cassandra")

  lazy val cassandraConfig: SparkConf = new SparkConf()
    // .setAppName("Structured_Streaming from Kafka to Cassandra")
     .set("spark.cassandra.connection.host", "localhost:9042")
    // .set("spark.cassandra.connection.host", cassandraConf.getString("brokers"))
    .set("spark.sql.streaming.checkpointLocation", cassandraConf.getString("checkpointLocation"))

  val sparkSession: SparkSession = SparkSession.builder()
    .master(sparkConfig.getString("MASTER_URL"))
    .appName("JsonMarketDataSalesStream")
    //.config(cassandraConfig)
    .getOrCreate()

  sparkSession.sparkContext.setLogLevel("WARN")

  val jsonStream: DataFrame = KafkaToSparkModels
    .KafkaToSpark(sparkSession)
    .readJsonStream(TickStructType
    .schema)

  jsonStream.createOrReplaceTempView("streaming_table")

  val queryStream: DataFrame  = sparkSession.sql( StructureWindowUdf.aggregateMarketData  )

  val sink: StreamingQuery = queryStream
    .writeStream
    .queryName("KafkaToCassandraForeach")
    .outputMode("update")
    .foreach(CassandraSinkForeach(sparkSession,  "db" ,  "market_data",
      CassandraTableWriterModels.OhlcTimeWindowsWriter)).start()

  sink.awaitTermination()
}






