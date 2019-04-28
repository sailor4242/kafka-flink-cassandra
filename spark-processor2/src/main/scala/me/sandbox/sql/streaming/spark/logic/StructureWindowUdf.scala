package me.sandbox.sql.streaming.spark.logic

 import me.sandbox.sql.streaming.spark.udf.EventAggregationUdf
 import org.apache.spark.sql.DataFrame
 import org.apache.spark.sql.functions.{window => byWindow, _}

 import scala.concurrent.duration.Duration

 // intermediate validation
object StructureWindowUdf {

  def aggregateMarketData: String = {
    """
      | select (window) as bar_time ,
      | FIRST_VALUE(tickerType) as ticker ,
      | FIRST_VALUE(last.value) as open ,
      | MAX(last.value) as high ,
      | MIN(last.value) as low ,
      | LAST_VALUE(last.value) as close ,
      | sum(volume) as vol ,
      | FIRST_VALUE(last.currency) as curr
      | from streaming_table
      | group by window(time, '30 seconds')
      | """.stripMargin
  }

  def findTickerCandidate(
    input: DataFrame,
    window: Duration,
    slide: Duration,
    watermark: Duration,
    minEvents: Long,
    maxEvents: Long,
    minRate: Double): DataFrame = {
    val aggregated =
      input
        .withWatermark("eventTime", watermark.toString)
        .groupBy(
          col("ticker"),
          byWindow(col("eventTime"), window.toString, slide.toString))
        .agg(EventAggregationUdf(col("action"), col("eventTime")).alias("aggregation"))

    aggregated
      .withColumn("total_events", col("aggregation.clicks") + col("aggregation.watches"))
      .filter(col("total_events") > minEvents)
      .withColumn(
        "rate",
        when(col("aggregation.clicks") > 0, col("aggregation.watches") / col("aggregation.clicks"))
          .otherwise(col("aggregation.watches")))
      .withColumn(
        "incident",
        when(
          col("total_events") >= maxEvents,
          concat(
            lit("too much events: "),
            col("total_events"),
            lit(" from "),
            col("aggregation.firstEvent"),
            lit(" to "),
            col("aggregation.lastEvent"))).when(
            col("rate") <= minRate,
            concat(
              lit("too suspicious rate: "),
              col("rate"),
              lit(" from "),
              col("aggregation.firstEvent"),
              lit(" to "),
              col("aggregation.lastEvent"))).otherwise(null))
      .filter(col("incident").isNotNull)
      .select(col("ip"), col("aggregation.lastEvent").as("lastEvent"), col("incident").as("reason"))
  }
}
