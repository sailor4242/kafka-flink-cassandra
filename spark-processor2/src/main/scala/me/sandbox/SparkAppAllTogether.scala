package me.sandbox



import com.typesafe.config.Config
import me.sandbox.sql.streaming.model.TickStructType
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions.from_json
import com.typesafe.config.ConfigFactory
import me.sandbox.sql.streaming.sink.cassandra.{CassandraSinkForeach, CassandraTableWriterModels}
import me.sandbox.sql.streaming.sink.kafka.MessagePublisher
import me.sandbox.sql.streaming.spark.logic.StructureWindowUdf
import org.apache.spark.sql.streaming.StreamingQuery



object AppSpark  extends App {
  // // Set up Kafka brokers and KafkaProducer to send messages to brokers
  MessagePublisher
  // // Set up Spark session to read stream (consume) from kafka brokers,
  // // do any transformations/processing
  println("it will take more than 1 minute before first result of apache spark appear ( after" +
    " start ). You can watch spark job at localhost:4040. Don't forget to check the log " +
    "activite of the microservice 'WebSocketClientToKafka' - and restart it if needed")
  println(" the spark healty activity will be look like ' Process new [[2019-02-14 09:59:30.0," +
    "2019-02-14 10:00:00.0],FXUS,3.225,3.225,3.225,3.225,1,RUB]' in white on the console ")
  SparkStructuredStreamer
}

object SparkStructuredStreamer {

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



  val queryStream: DataFrame  = sparkSession.sql( StructureWindowUdf.aggregateMarketData  )


  val sink: StreamingQuery = queryStream
    .writeStream
    .queryName("KafkaToCassandraForeach")
    .outputMode("update")
    .foreach(CassandraSinkForeach(sparkSession,  "db" ,  "market_data",
      CassandraTableWriterModels.OhlcTimeWindowsWriter)).start()

  sink.awaitTermination()

}






