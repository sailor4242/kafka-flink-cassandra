
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{ Properties, UUID }

import com.typesafe.scalalogging.Logger
import io.circe.parser.decode
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.{ DataStream, StreamExecutionEnvironment, _ }
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import pureconfig.loadConfigOrThrow
import org.apache.flink.streaming.api.datastream._
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor
import org.apache.flink.streaming.connectors.cassandra.CassandraSink
import pureconfig.generic.auto._

import io.circe.parser.decode
import model.Ticker
//import org.apache.flink.streaming.connectors.cassandra.CassandraSink

import io.circe.parser.decode
import model.Ticker
//import org.apache.flink.streaming.connectors.cassandra.CassandraSink

/**
 * Reads from kafka topic and processes it using flink in windows
 */
object FlinkProcessTopic extends App {
  // Some general config to get the actor system up and running
  val className = this.getClass.getName.split("\\$").last
  val streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  streamExecutionEnvironment.setParallelism(1)
  implicit lazy val config: FlinkConfig = loadConfigOrThrow[FlinkConfig]
  implicit val logger: Logger = Logger(className)

  // Set Flink properties
  val properties = new Properties()
  properties.setProperty("bootstrap.servers", config.kafkaConfig.port)
  properties.setProperty("group.id", config.kafkaConfig.groupId)

  val stream = streamExecutionEnvironment
    .addSource(
      new FlinkKafkaConsumer011[String]("FXUS", new SimpleStringSchema(),
        properties)
        .setStartFromEarliest())
  val tickerStream: DataStream[Ticker] = stream.map { str =>
    val tick = decode[Ticker](str).right.get
    tick
  }.assignTimestampsAndWatermarks(new AscendingTimestampExtractor[Ticker]() {
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override def extractAscendingTimestamp(element: Ticker): Long = {
      val ts = new Timestamp(sdf.parse(element.time).getTime)
      //println(ts.getTime)
      ts.getTime
    }
  })
  tickerStream.print()

  val cassandraTickersStream = tickerStream.map { tick =>
    TickerCassandra(
      UUID.randomUUID(),
      tick.tickerType.toString,
      tick.time,
      tick.last.value.toString,
      tick.volume,
      tick.last.currency.toString)
  }

  val bar1MinStream: DataStream[OHLC1Min] = tickerStream
    .timeWindowAll(Time.minutes(1)).process(FlinkWindowProcessFunc)
  bar1MinStream.print()

  CassandraSink.addSink(bar1MinStream)
    .setHost(config.cassandraConfig.host, config.cassandraConfig.port)
    .setQuery("INSERT INTO " +
      "test_keyspace.table_ohlc1m(id, ticker, start_time, open, high, low, close_time, volume, " +
      "currency) " +
      "values (?, ?, ?, ?, ?, ?, ?, ?, ? );").build()

  CassandraSink.addSink(cassandraTickersStream)
    .setHost(config.cassandraConfig.host, config.cassandraConfig.port)
    .setQuery("INSERT INTO test_keyspace" +
      ".table_ticker(id, ticker, time, price, volume, currency )" +
      "values (?, ?, ?, ?, ?, ?);").build()

  streamExecutionEnvironment.execute(config.jobName)
}

