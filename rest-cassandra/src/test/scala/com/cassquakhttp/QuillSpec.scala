/*
package com.cassquakhttp

import java.util.UUID

import com.datastax.driver.core.{ Cluster, PoolingOptions }
import io.getquill.{ CassandraAsyncContext, SnakeCase }
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, MustMatchers, WordSpec }
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext.Implicits.global
import io.getquill._

class QuillSpec extends WordSpec
  with ScalaFutures
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with MustMatchers {

  //Config Object can be passed in to the Context object which is a good thing as specific configs could be used.
  //https://docs.datastax.com/en/developer/java-driver/2.1/manual/pooling/

  val poolingOptions = new PoolingOptions

  val cluster = Cluster.builder
    .addContactPoint("127.0.0.1")
    .withPort(9042)
    .withPoolingOptions(poolingOptions)
    .build

  val db = new CassandraAsyncContext(SnakeCase, cluster, "db", 1000)

  import db._

  case class WeatherStation(country: String, city: String, stationId: String, entry: Int, value: Int)

  object WeatherStation {

    val getAll = quote {
      query[WeatherStation].map(ws => ws)
    }

    val getAllByCountry = quote {
      country: String => query[WeatherStation].filter(_.country == country)
    }

    def insert(weatherStations: List[WeatherStation]) = quote {
      liftQuery(weatherStations).foreach(c => query[WeatherStation].insert(c))
    }

    def insert = quote {
      ws: WeatherStation => query[WeatherStation].insert(ws)
    }

    def delete = quote {
      query[WeatherStation].delete
    }

  }

  val weatherStations = List(
    WeatherStation("CO", "Bogotá", UUID.randomUUID().toString, r.nextInt(), r.nextInt),
    WeatherStation("DE", "Berlin", UUID.randomUUID().toString, r.nextInt(), r.nextInt))

  override protected def beforeEach(): Unit = {
    await(db.run(WeatherStation.delete))
    await(db.run(WeatherStation.insert(weatherStations)))
  }

  override def afterAll(): Unit = {
    db.close()
  }

  "QuillSpec" must {

    "do simple query" in {

      val result = db.run(WeatherStation.getAllByCountry(lift("UK")))

      await(result) mustEqual Nil

    }

    "do simple query to expect one value" in {

      val result = db.run(WeatherStation.getAllByCountry(lift("CO")))

      await(result).map(_.country) mustEqual List("CO")

    }

    "expecting a list length after query" in {

      val result = db.run(WeatherStation.getAll)

      await(result).length mustEqual weatherStations.length

    }

  }

}
*/
