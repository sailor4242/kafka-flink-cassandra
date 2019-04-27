/*
package com.cassquakhttp

import java.util.{ Date, UUID }

import com.datastax.driver.core.{ Cluster, PoolingOptions }
import com.google.inject.{ AbstractModule, Guice }
import io.getquill.{ CassandraAsyncContext, NamingStrategy, SnakeCase }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, MustMatchers, WordSpec }
import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global

trait ClusterService {
  val poolingOptions: PoolingOptions
  val cluster: Cluster
}

@Singleton
class DefaultClusterService extends ClusterService {

  override val poolingOptions = new PoolingOptions

  override val cluster = Cluster.builder
    .addContactPoint("127.0.0.1")
    .withPort(9042)
    .withPoolingOptions(poolingOptions)
    .build

}

trait ConnectionService {
  type N <: NamingStrategy
  val context: CassandraAsyncContext[N]

}

@Singleton
class DefaultConnectionService @Inject() (clusterService: ClusterService) extends ConnectionService {

  override type N = SnakeCase.type

  override val context =
    new CassandraAsyncContext(
      SnakeCase,
      clusterService.cluster,
      "db",
      1000)

}

class ServiceBinder extends AbstractModule {

  override def configure() = {

    bind(classOf[ClusterService]).to(classOf[DefaultClusterService])
    bind(classOf[ConnectionService]).to(classOf[DefaultConnectionService])

  }

}

class ClusterDISpec extends WordSpec
  with ScalaFutures
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with MustMatchers {

  val serviceInjector = Guice.createInjector(new ServiceBinder())

  "Cluster and Cassandra Context" must {

    "be able to get proper instance and do query" in {

      val connectionService = serviceInjector.getInstance(classOf[ConnectionService])

      val db = connectionService.context

      import db._

      implicit val eventSchemaMeta = schemaMeta[Event]("events")

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

      assert(await(db.run(quote(query[Event]))).nonEmpty)
    }

    "be able to get proper instance and do query without recreating it" in {

      val connectionService = serviceInjector.getInstance(classOf[ConnectionService])

      val db = connectionService.context

      import db._

      implicit val eventSchemaMeta = schemaMeta[Event]("events")

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

      assert(await(db.run(quote(query[Event]))).nonEmpty)
    }

  }

  override protected def afterAll(): Unit = {

    val connectionService = serviceInjector.getInstance(classOf[ConnectionService])

    val db = connectionService.context

    db.close()
  }
}
*/
