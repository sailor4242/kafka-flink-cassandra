/*
package com.cassquakhttp

import java.util.{ Date, UUID }

import io.getquill.context.cassandra.CassandraContext
import io.getquill.context.cassandra.encoding.{ Decoders, Encoders }
import io.getquill.{ CassandraAsyncContext, SnakeCase }
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, MustMatchers, WordSpec }

import scala.concurrent.ExecutionContext.Implicits.global

trait EventDAOBase {

  val db: CassandraContext[_] with Encoders with Decoders
  import db._

  case class Event(
    id: UUID,
    principal: String,
    category: String,
    eventSourceService: String,
    deviceId: UUID,
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int,
    milli: Int,
    created: Date,
    updated: Date)

  trait TablePointer[T] {
    implicit val eventSchemaMeta: SchemaMeta[T]
  }

  object Events extends TablePointer[Event] {

    implicit val eventSchemaMeta = schemaMeta[Event]("events")

    def selectAll = quote(query[Event])

    def byPrincipalAndCategory(principal: String, category: String) = quote {
      query[Event]
        .filter(_.principal == lift(principal))
        .filter(_.category == lift(category))
    }

    def byPrincipalAndCatAndYear(principal: String, category: String, date: DateTime) = quote {
      query[Event]
        .filter(_.principal == lift(principal))
        .filter(_.category == lift(category))
        .filter(_.year == lift(date.year().get()))
    }

    def byPrincipalAndCatAndYearAndMonth(principal: String, category: String, date: DateTime) = quote {
      query[Event]
        .filter(_.principal == lift(principal))
        .filter(_.category == lift(category))
        .filter(_.year == lift(date.year().get()))
        .filter(_.month == lift(date.monthOfYear().get()))
    }

    def byPrincipalAndCatAndYearAndMonthAndDay(principal: String, category: String, date: DateTime) = quote {
      query[Event]
        .filter(_.principal == lift(principal))
        .filter(_.category == lift(category))
        .filter(_.year == lift(date.year().get()))
        .filter(_.month == lift(date.monthOfYear().get()))
        .filter(_.day == lift(date.dayOfMonth().get()))
    }

  }

  object EventsByCat extends TablePointer[Event] {

    implicit val eventSchemaMeta = schemaMeta[Event]("events_by_cat")

    def selectAll = quote(query[Event])

    def byCatAndEventSourceAndYearAndMonth(category: String, eventSourceService: String, date: DateTime) = quote {
      query[Event]
        .filter(_.category == lift(category))
        .filter(_.eventSourceService == lift(eventSourceService))
        .filter(_.year == lift(date.year().get()))
        .filter(_.month == lift(date.monthOfYear().get()))
    }

    def byCatAndEventSourceAndYearAndMonthAndDay(category: String, eventSourceService: String, date: DateTime) = quote {
      query[Event]
        .filter(_.category == lift(category))
        .filter(_.eventSourceService == lift(eventSourceService))
        .filter(_.year == lift(date.year().get()))
        .filter(_.month == lift(date.monthOfYear().get()))
        .filter(_.day == lift(date.dayOfMonth().get()))
    }

    def byCatAndEventSourceAndYearAndMonthAndDayAndDeviceId(category: String, eventSourceService: String, date: DateTime, deviceId: UUID) = quote {
      query[Event]
        .filter(_.category == lift(category))
        .filter(_.eventSourceService == lift(eventSourceService))
        .filter(_.year == lift(date.year().get()))
        .filter(_.month == lift(date.monthOfYear().get()))
        .filter(_.day == lift(date.dayOfMonth().get()))
        .filter(_.hour == lift(date.hourOfDay().get()))
        .filter(_.deviceId == lift(deviceId))

    }

  }

  val events = List(
    Event(UUID.fromString("b5be9010-ed0e-11e8-82a0-53d40fda30be"), "RMunichRET", "Anchor", "Avatar-Service", UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"), 2018, 11, 2, 11, 17, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5bb34b0-ed0e-11e8-82a0-53d40fda30be"), "Regio IT", "Anchor", "Avatar-Service", UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"), 2018, 11, 2, 8, 17, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5b456e0-ed0e-11e8-82a0-53d40fda30be"), "Regio IT", "Anchor", "Avatar-Service", UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"), 2018, 11, 1, 7, 17, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5bda5b0-ed0e-11e8-82a0-53d40fda30be"), "MunichRE", "Anchor", "Avatar-Service", UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"), 2018, 11, 1, 9, 17, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5b51a30-ed0e-11e8-82a0-53d40fda30be"), "Regio IT", "Validate", "Avatar-Service", UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"), 2018, 11, 2, 8, 15, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5a38e00-ed0e-11e8-82a0-53d40fda30be"), "Regio IT", "Validate", "Avatar-Service", UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"), 2018, 11, 1, 7, 15, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5be1ae0-ed0e-11e8-82a0-53d40fda30be"), "MunichRE", "Validate", "Avatar-Service", UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"), 2018, 11, 2, 11, 15, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5bbd0f0-ed0e-11e8-82a0-53d40fda30be"), "MunichRE", "Validate", "Avatar-Service", UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"), 2018, 10, 1, 9, 15, 0, 0, new Date(), new Date()))

  val eventsByCat = List(
    Event(UUID.fromString("b5be9010-ed0e-11e8-82a0-53d40fda30be"), "RMunichRET", "Anchor", "Avatar-Service", UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"), 2018, 11, 2, 11, 17, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5bb34b0-ed0e-11e8-82a0-53d40fda30be"), "Regio IT", "Anchor", "Avatar-Service", UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"), 2018, 11, 2, 8, 17, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5b456e0-ed0e-11e8-82a0-53d40fda30be"), "Regio IT", "Anchor", "Avatar-Service", UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"), 2018, 11, 1, 7, 17, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5bda5b0-ed0e-11e8-82a0-53d40fda30be"), "MunichRE", "Anchor", "Avatar-Service", UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"), 2018, 11, 1, 9, 17, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5b51a30-ed0e-11e8-82a0-53d40fda30be"), "Regio IT", "Validate", "Avatar-Service", UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"), 2018, 11, 2, 8, 15, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5a38e00-ed0e-11e8-82a0-53d40fda30be"), "Regio IT", "Validate", "Avatar-Service", UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"), 2018, 11, 1, 7, 15, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5be1ae0-ed0e-11e8-82a0-53d40fda30be"), "MunichRE", "Validate", "Avatar-Service", UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"), 2018, 11, 2, 11, 15, 0, 0, new Date(), new Date()),
    Event(UUID.fromString("b5bbd0f0-ed0e-11e8-82a0-53d40fda30be"), "MunichRE", "Validate", "Avatar-Service", UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"), 2018, 10, 1, 9, 15, 0, 0, new Date(), new Date()))

}

class QuillDifferentTableVersionsSpec extends WordSpec
  with ScalaFutures
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with MustMatchers
  with EventDAOBase {

  val db = new CassandraAsyncContext(SnakeCase, "db")

  import db._

  override def afterAll(): Unit = {
    db.close()
  }

  "Events" must {

    "select * from events;" in {

      val runQuery = await(db.run(Events.selectAll))

      runQuery.map(_.deviceId) must contain theSameElementsInOrderAs events.map(_.deviceId)

    }

    "select * from events where principal = 'Regio IT' and category = 'Anchor';" in {

      val runQuery = await {
        db.run {
          Events.byPrincipalAndCategory(
            "Regio IT",
            "Anchor")
        }
      }

      val expected = List(
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"),
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"))

      runQuery.map(_.deviceId) must contain theSameElementsInOrderAs expected

    }

    "select * from events where principal = 'Regio IT' and category = 'Anchor' and year = 2018;" in {

      val date = new DateTime().withYear(2018)

      val runQuery = await {
        db.run {
          Events.byPrincipalAndCatAndYear(
            "Regio IT",
            "Anchor",
            date)
        }
      }

      val expected = List(
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"),
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"))

      runQuery.map(_.deviceId) must contain theSameElementsInOrderAs expected

    }

    "select * from events where principal = 'Regio IT' and category = 'Anchor' and year = 2018 and month = 11;" in {

      val date = new DateTime()
        .withYear(2018)
        .withMonthOfYear(11)

      val runQuery = await {
        db.run {
          Events.byPrincipalAndCatAndYearAndMonth(
            "Regio IT",
            "Anchor",
            date)
        }
      }

      val expected = List(
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"),
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"))

      runQuery.map(_.deviceId) must contain theSameElementsInOrderAs expected

    }

    "select * from events where principal = 'Regio IT' and category = 'Anchor' and year = 2018 and month = 11 and day = 1;" in {

      val date = new DateTime()
        .withYear(2018)
        .withMonthOfYear(11)
        .withDayOfMonth(1)

      val runQuery = await {
        db.run {
          Events.byPrincipalAndCatAndYearAndMonthAndDay(
            "Regio IT",
            "Anchor",
            date)
        }
      }

      val expected = List(
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"))

      runQuery.map(_.deviceId) must contain theSameElementsInOrderAs expected

    }

  }

  "EventsByCat" must {

    "select * from events_by_cat;" in {

      val runQuery = await(db.run(EventsByCat.selectAll))

      val expected = List(
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"),
        UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"),
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"),
        UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"),
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"),
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"),
        UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"),
        UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"))

      runQuery.map(_.deviceId) must contain theSameElementsInOrderAs expected

    }

    "select * from events_by_cat where category = 'Anchor' and event_source_service = 'Avatar-Service' and year = 2018 and month = 11;" in {

      val date = new DateTime()
        .withYear(2018)
        .withMonthOfYear(11)

      val runQuery = await {
        db.run {
          EventsByCat.byCatAndEventSourceAndYearAndMonth(
            "Anchor",
            "Avatar-Service",
            date)
        }
      }

      val expected = List(
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"),
        UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"),
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"),
        UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"))

      runQuery.map(_.deviceId) must contain theSameElementsInOrderAs expected

    }

    "select * from events_by_cat where category = 'Anchor' and event_source_service = 'Avatar-Service' and year = 2018 and month = 11 and day = 2;" in {

      val date = new DateTime()
        .withYear(2018)
        .withMonthOfYear(11)
        .withDayOfMonth(2)

      val runQuery = await {
        db.run {
          EventsByCat.byCatAndEventSourceAndYearAndMonthAndDay(
            "Anchor",
            "Avatar-Service",
            date)
        }
      }

      val expected = List(
        UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"),
        UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"))

      runQuery.map(_.deviceId) must contain theSameElementsInOrderAs expected

    }

    "select * from events_by_cat where category = 'Anchor' and event_source_service = 'Avatar-Service' and year = 2018 and month = 11 and day = 2 and hour = 11 and device_id = 41245902-69a0-450c-8d37-78e34f0e6760;" in {

      val date = new DateTime()
        .withYear(2018)
        .withMonthOfYear(11)
        .withDayOfMonth(2)
        .withHourOfDay(11)

      val runQuery = await {
        db.run {
          EventsByCat.byCatAndEventSourceAndYearAndMonthAndDayAndDeviceId(
            "Anchor",
            "Avatar-Service",
            date,
            UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"))
        }
      }

      val expected = List(UUID.fromString("41245902-69a0-450c-8d37-78e34f0e6760"))

      runQuery.map(_.deviceId) must contain theSameElementsInOrderAs expected

    }

  }

}
*/
