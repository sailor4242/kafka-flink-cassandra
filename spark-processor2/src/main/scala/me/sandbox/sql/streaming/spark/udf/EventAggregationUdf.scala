package me.sandbox.sql.streaming.spark.udf

import org.apache.spark.sql.Row
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types._

object EventAggregationUdf extends UserDefinedAggregateFunction {

  override def inputSchema: StructType =
    StructType(
      StructField("action", StringType) ::
        StructField("eventTime", TimestampType) ::
        Nil)

  override def bufferSchema: StructType =
    StructType(
      StructField("time", LongType) ::
        StructField("price", FloatType) ::
        StructField("firstEvent", TimestampType) ::
        StructField("lastEvent", TimestampType) ::
        Nil)

  override def dataType: DataType = bufferSchema

  override def deterministic: Boolean = true

  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = 0L
    buffer(1) = 0L
  }

  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    /* aggregate count clicks and watches */
    if (input(0) == "time") {
      buffer(0) = buffer.getLong(0) + 1L
    } else if (input(0) == "price") {
      buffer(1) = buffer.getLong(1) + 1L
    } else {}

    val actionEvent = input.getTimestamp(1)
    if (!buffer.isNullAt(2) && !buffer.isNullAt(3)) {
      /* firstEvent min actionEvent */
      if (actionEvent.before(buffer.getTimestamp(2))) buffer(2) = actionEvent

      /* lastEvent max actionEvent */
      if (actionEvent.after(buffer.getTimestamp(3))) buffer(3) = actionEvent
    } else {
      buffer(2) = actionEvent
      buffer(3) = actionEvent
    }
  }

  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    /* aggregate count clicks and watches */
    buffer1(0) = buffer1.getLong(0) + buffer2.getLong(0)
    buffer1(1) = buffer1.getLong(1) + buffer2.getLong(1)

    /* firstEvent is min or nothing changes */
    if (!buffer1.isNullAt(2) && !buffer2.isNullAt(2)) {
      val l = buffer1.getTimestamp(2)
      val r = buffer2.getTimestamp(2)
      if (r.before(l)) {
        buffer1(2) = r
      }
    } else if (!buffer2.isNullAt(2)) {
      buffer1(2) = buffer2.getTimestamp(2)
    }

    /* lastEvent is max or nothing changes */
    if (!buffer1.isNullAt(3) && !buffer2.isNullAt(3)) {
      val l = buffer1.getTimestamp(3)
      val r = buffer2.getTimestamp(3)
      if (r.after(l)) {
        buffer1(3) = r
      }
    } else if (!buffer2.isNullAt(3)) {
      buffer1(3) = buffer2.getTimestamp(3)
    }
  }

  override def evaluate(buffer: Row): Any = buffer
}
