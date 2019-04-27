/*
package com.cassquakhttp

import java.util
import java.util.{ Date, UUID }

import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, MustMatchers, WordSpec }
import io.getquill.context.mirror.Row
import io.getquill._
import io.getquill.CassandraMirrorContext
import com.datastax.driver.core.utils.UUIDs

trait EventDAOMirrorBase {

  val mirrorDB = new CassandraMirrorContext(Literal)
  import mirrorDB._

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

    def selectAll(implicit sm: SchemaMeta[Event]) = quote(query[Event])

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

    def selectAll(implicit sm: SchemaMeta[Event]) = quote(query[Event])

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

    def insertEvent(event: Event) = quote {
      query[Event].insert(lift(event))
    }

  }

}

class QuillMirrorSpec extends WordSpec
  with ScalaFutures
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with MustMatchers
  with EventDAOMirrorBase {

  import mirrorDB._

  "Events Statements" must {

    "mirror select * from events;" in {

      val runQuery = mirrorDB.run(Events.selectAll).string

      assert(runQuery == "SELECT id, principal, category, eventSourceService, deviceId, year, month, day, hour, minute, second, milli, created, updated FROM events")

    }

    "mirror select * from events where principal = 'Regio IT' and category = 'Anchor';" in {

      val runQuery =
        mirrorDB.run {
          Events.byPrincipalAndCategory(
            "Regio IT",
            "Anchor")
        }

      val expected = "SELECT id, principal, category, eventSourceService, deviceId, year, month, day, hour, minute, second, milli, created, updated FROM events WHERE principal = ? AND category = ?"

      assert(runQuery.string == expected)
      assert(runQuery.prepareRow == Row("Regio IT", "Anchor"))

    }

  }

  "Events By Cats Statements" must {

    "mirror select * from events_by_cat;" in {

      val runQuery = mirrorDB.run(EventsByCat.selectAll)

      val expected = "SELECT id, principal, category, eventSourceService, deviceId, year, month, day, hour, minute, second, milli, created, updated FROM events_by_cat"

      assert(runQuery.string == expected)

    }

    "mirror select * from events_by_cat where category = 'Anchor' and event_source_service = 'Avatar-Service' and year = 2018 and month = 11;" in {

      val date = new DateTime()
        .withYear(2018)
        .withMonthOfYear(11)

      val runQuery =
        mirrorDB.run {
          EventsByCat.byCatAndEventSourceAndYearAndMonth(
            "Anchor",
            "Avatar-Service",
            date)
        }

      val expected = "SELECT id, principal, category, eventSourceService, deviceId, year, month, day, hour, minute, second, milli, created, updated FROM events_by_cat WHERE category = ? AND eventSourceService = ? AND year = ? AND month = ?"

      assert(runQuery.string == expected)
      assert(runQuery.prepareRow == Row("Anchor", "Avatar-Service", 2018, 11))

    }

    "mirror insert insert into events_by_cat (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'Regio IT', 'Validate', 'Avatar-Service', 522f3e64-6ee5-470c-8b66-9edb0cfbf3b1, 2018, 11, 1, 7, 15, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));" in {

      val id = UUIDs.timeBased
      val date = new Date()

      val expected = "INSERT INTO events_by_cat (id,principal,category,eventSourceService,deviceId,year,month,day,hour,minute,second,milli,created,updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

      val toInsert = Event(
        id = id,
        principal = "Regio IT",
        category = "Validate",
        eventSourceService = "Avatar-Service",
        deviceId = util.UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"),
        year = 2018,
        month = 11,
        day = 1,
        hour = 7,
        minute = 15,
        second = 0,
        milli = 0,
        created = date,
        updated = date)

      val query = mirrorDB.run(EventsByCat.insertEvent(toInsert))

      assert(query.string == expected)
      assert(query.prepareRow == Row(id, "Regio IT", "Validate", "Avatar-Service", UUID.fromString("522f3e64-6ee5-470c-8b66-9edb0cfbf3b1"), 2018, 11, 1, 7, 15, 0, 0, date, date))

    }

  }

}

*/
