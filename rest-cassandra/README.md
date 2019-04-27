# ubirch-cassandra-eval
Evaluation of Cassandra and Scala

Integration Tools

* [Getting Started](#getting-started)

* [Alpakka](#alpakka-cassandra-connector)

* [Quill](#quill)

* [Phantom](#phantom)

[DB migrations management](#db-migrations-management)

[Guice DI and Quill Context](#guice-di-and-quill-context)

[Macwire DI/Trait and Quill Context](#macwire-di-and-quill-context)


### TL;TR

_High-level Connectors_

Technology | Documentation  | Licence | Case Class Support | Streams Support | Async Support | Scala-like Idioms
------------ | ------------- | ------------- | ------------- | ------------- | -------------  | -------------  
Alpakka | OK | Open Source: Apache 2 License | Needs explicit binding | Built on top of Akka-Streams | Yes | Yes
Quill | OK | Open Source: Apache 2 License | Out of the box support | Through Monix Observables | Yes | Yes
Phantom | OK |  Apache V2 License. Other Components are also [licenced](https://github.com/outworkers/phantom#license-and-copyright) (phantom-pro) | Supports it but needs explicit table class definition |  play-iteratees | Yes | Almost

_DB Migration Tools_

Technology | Language | Execution Mode | Has Migration folder | Incremental File Nomenclature (e.g 1.cql, 2.cql, ..., n.cql) | Filename-based order | cql statements support | Documentation | Heartbeat | Driver Version Dep.    
---------- | -------| -------------- | ---------------------| ------------------------------------------------------------ | -------------------- | ---------------------- | ------------  |  ---------- | ---------- 
https://github.com/patka/cassandra-migration | Java | * Migration Execution code needs to be put somewhere in the app when it boots | Yes | Yes | Yes | Yes | Basic | Last commit was 4 months ago: Seems like they are getting ready for a new cycle | 3.X 
https://github.com/Contrast-Security-OSS/cassandra-migration | Java | * Migration Execution code needs to be put somewhere in the app when it boots. It also has a java-based CLI| Yes | Yes | Yes | Yes | Basic | Last commit was 3 years ago: **Red flag** | 2.1: Found version conflict(s) in library dependencies; some are suspected to be binary incompatible 
https://github.com/smartcat-labs/cassandra-migration-tool-java | Java | * Migration Execution code needs to be put somewhere in the app when it boots. When using the moving data feature, that could take a lot of time. This feature is interesting, but I'd recommend and independent app to do this. | Yes | No | No | Yes | Basic | Last commit was 2 years ago: **Red flag** | 3.1: dependency conflicts, but solved 
https://github.com/Cobliteam/cassandra-migrate | Python | It's a CLI | Yes | Yes | Yes | Yes | Basic | Last commit was 1 year ago | Python driver: cassandra-driver-3.16.0 
https://github.com/joeledwards/node-cassandra-migration | Javascript | It's a CLI | Yes | Yes | Yes | Yes | Basic | Last commit was 5 months ago | Uses the Javascript driver 
https://github.com/o19s/trireme/ | Python | It's a CLI | Yes | Yes | Yes | Yes | Basic | Last commit was 2 years ago | Python driver cassandra-driver-3.16.0

\* The recommended way to handle these migrations would be through one simple app that controls the db migrations.

*Notes*

* Quill seems to fit well and easily as far as usage and configuration.
* The db migrations tools that fit well and are maintained more regularly are:

    * https://github.com/Cobliteam/cassandra-migrate
    * https://github.com/patka/cassandra-migration
    
  These two options have:
  
    - [X] Folder with something like 1.cql, 2.cql, …
    - [X] Migration Table
        - [X] Informative Tables with checksums and order/version and timestamps.
    - [X] Order is defined by sorting filenames
    - [X] CQSL statements support in .cql files

    

## Getting Started

* [Apache Cassandra](http://cassandra.apache.org/)

    Make sure you have a running instance of [Apache Cassandra](http://cassandra.apache.org/). You can follow the install instructions [here](http://cassandra.apache.org/doc/latest/getting_started/installing.html).
    At the time of this writing, the version being used is _3.11.3_ 
    
* [SPT](https://www.scala-sbt.org/)

    Make sure you have installed _sbt_    


Once you have cloned the project, you can follow the instructions below:

```
sbt compile
```

This command should compile all the sub-projects.
 
You can also run all the tests by running 

```
sbt test
``` 

But if you do so, make sure the prerequisites of all the subprojects are met, otherwise, you will see a lot of failed tests.

Alternatively, you can dive into the project you would like to run tests for or run its examples by doing. Don't forget the project's requisites.

```
sbt 
sbt:ubirch-cassandra-eval> project  PROJECT_NAME
test or run 

``` 

## Alpakka-Cassandra Connector

**Page**: https://developer.lightbend.com/docs/alpakka/current/cassandra.html

**Description**: The Cassandra connector allows you to read and write to Cassandra. You can query a stream of rows from CassandraSource or use prepared statements to insert or update with CassandraFlow or CassandraSink.

**Prerequisites:** A running instance of [Apache Cassandra](http://cassandra.apache.org/)

**Notes:**

* Akka Streams are first-class citizens.
* Under the hood, it uses [https://github.com/datastax/java-driver](https://github.com/datastax/java-driver) as the driver.
* There's no implicit support for binding case classes when building the db statements. 
  This means that every case class must be explicitly mapped into the bind function.
  So, in the case of complex case classes and cassandra tables, the maintenance of the bind function can be cumbersome and brittle.
* If the principal data was in a _Json_ format, and only the fields that would be used for querying are part of the case class,
  then the process should be easier and better controlled.
* Something in line with the previous point is that the db statements are raw queries, which again, are prone to maintenance errors.
* It is not clear if it supports statement caching.
* The composability of queries is almost none existing as queries are raw queries.

### How to run

```
1. Start your Cassandra.
2. run 'sbt'
3. Select project by running 'project alpakka' 
4. run test
```

## Quill

**Page**: http://getquill.io/

**Description**: Free/Libre Compile-time Language Integrated Queries for Scala

**Prerequisites:** 

* A running instance of [Apache Cassandra](http://cassandra.apache.org/)
* Make sure you have run this on your db.

```
CREATE KEYSPACE IF NOT EXISTS db
WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE TABLE IF NOT EXISTS db.weather_station (
  country TEXT,
  city TEXT,
  station_id TEXT,
  entry INT,
  value INT,
  PRIMARY KEY (country, city, station_id)
);

CREATE TABLE IF NOT EXISTS db.traffic_light_sensor (
  country TEXT,
  city TEXT,
  sensor_id TEXT,
  entry INT,
  value INT,
  PRIMARY KEY (country, city, sensor_id)
);

CREATE TABLE db.sensor_failures_count (
  id UUID PRIMARY KEY,
  failures counter
  );
  
  
drop table if exists db.events;

create table if not exists db.events (
    id timeuuid ,
    principal text ,
    category text ,
    event_source_service text ,
    device_id UUID,
    year int ,
    month int ,
    day int ,
    hour int ,
    minute int ,
    second int,
    milli int,
    created timestamp,
    updated timestamp, 
    PRIMARY KEY ((principal, category), year, month, day, hour, device_id)
) WITH CLUSTERING ORDER BY (year desc, month DESC, day DESC);

drop table if exists db.events_by_cat;

create table if not exists db.events_by_cat (
    id timeuuid ,
    principal text ,
    category text ,
    event_source_service text ,
    device_id UUID,
    year int ,
    month int ,
    day int ,
    hour int ,
    minute int ,
    second int,
    milli int,
    created timestamp,
    updated timestamp, 
    PRIMARY KEY ((category, event_source_service, year, month), day, hour, device_id)
);

insert into db.events (id, principal, category, event_source_service, device_id, year, month, day, 
hour, minute, second, milli, created, updated) values (now(), 'Regio IT', 'Validate', 'Avatar-Service', 522f3e64-6ee5-470c-8b66-9edb0cfbf3b1, 2018, 11, 1, 7, 15, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));
insert into db.events (id, principal, category, event_source_service, device_id, year, month, day, 
hour, minute, second, milli, created, updated) values (now(), 'Regio IT', 'Anchor', 'Avatar-Service', 522f3e64-6ee5-470c-8b66-9edb0cfbf3b1, 2018, 11, 1, 7, 17, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));
insert into db.events (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'Regio IT', 'Validate', 'Avatar-Service', 522f3e64-6ee5-470c-8b66-9edb0cfbf3b1, 2018, 11, 2, 8, 15, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));
insert into db.events (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'Regio IT', 'Anchor', 'Avatar-Service', 522f3e64-6ee5-470c-8b66-9edb0cfbf3b1, 2018, 11, 2, 8, 17, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));

insert into db.events (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'MunichRE', 'Validate', 'Avatar-Service', 41245902-69a0-450c-8d37-78e34f0e6760, 2018, 10, 1, 9, 15, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));
insert into db.events (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'MunichRE', 'Anchor', 'Avatar-Service', 41245902-69a0-450c-8d37-78e34f0e6760, 2018, 11, 1, 9, 17, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));
insert into db.events (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'MunichRE', 'Validate', 'Avatar-Service', 41245902-69a0-450c-8d37-78e34f0e6760, 2018, 11, 2, 11, 15, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));
insert into db.events (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'RMunichRET', 'Anchor', 'Avatar-Service', 41245902-69a0-450c-8d37-78e34f0e6760, 2018, 11, 2, 11, 17, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));

insert into db.events_by_cat (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'Regio IT', 'Validate', 'Avatar-Service', 522f3e64-6ee5-470c-8b66-9edb0cfbf3b1, 2018, 11, 1, 7, 15, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));
insert into db.events_by_cat (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'Regio IT', 'Anchor', 'Avatar-Service', 522f3e64-6ee5-470c-8b66-9edb0cfbf3b1, 2018, 11, 1, 7, 17, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));
insert into db.events_by_cat (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'Regio IT', 'Validate', 'Avatar-Service', 522f3e64-6ee5-470c-8b66-9edb0cfbf3b1, 2018, 11, 2, 8, 15, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));
insert into db.events_by_cat (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'Regio IT', 'Anchor', 'Avatar-Service', 522f3e64-6ee5-470c-8b66-9edb0cfbf3b1, 2018, 11, 2, 8, 17, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));

insert into db.events_by_cat (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'MunichRE', 'Validate', 'Avatar-Service', 41245902-69a0-450c-8d37-78e34f0e6760, 2018, 10, 1, 9, 15, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));
insert into db.events_by_cat (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'MunichRE', 'Anchor', 'Avatar-Service', 41245902-69a0-450c-8d37-78e34f0e6760, 2018, 11, 1, 9, 17, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));
insert into db.events_by_cat (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'MunichRE', 'Validate', 'Avatar-Service', 41245902-69a0-450c-8d37-78e34f0e6760, 2018, 11, 2, 11, 15, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));
insert into db.events_by_cat (id, principal, category, event_source_service, device_id, year, month, day, hour, minute, second, milli, created, updated) values (now(), 'RMunichRET', 'Anchor', 'Avatar-Service', 41245902-69a0-450c-8d37-78e34f0e6760, 2018, 11, 2, 11, 17, 0, 0, toUnixTimestamp(now()), toUnixTimestamp(now()));  
```

**Notes:**

* It supports case classes very nicely. In the case con composed/nested case classes, it offers a simple way to add from/to functions that allow the mapping.
* The schema fields can be customized to support different table names or field names. For example if a field in your case class is name, you can 
customize it to use "_name" against the db. This is a plus.
* Values from the db can be lifted to case classes.
* It support algebraic operators like map, filter, flatMap, etc., which is very nice too.
* Another interesting characteristic which is part of supporting case classes is that returning values can be ad-hoc case classes, this means that 
queries can return values that are not part of the db.
* It supports compiled queries, which could be a factor in performance.
* Something that looks very amazing is the IO Monad. I need to run some examples. But this is definitely, an interesting functional programming support.
* It also support UDTs.
* Something that is important to see is how you can organize your model so that you each model doesn't repeat definition of the Casssandra Context.
  An interesting question is, should we handle the queries inside the case class companion object or inside a DAO class or object.  Both options seem plausible.
* Might be worth looking at runtime or compile DI to handle the Quill Context. In some aspects this is related to a connection pool.

**Questions:**

1. How is the connection pool handled and how configurations are available.?

    It looks like a config object can be passed into the context?
    
    The context object supports the following constructors:
    
    ```
      def this(naming: N, config: CassandraContextConfig) = this(naming, config.cluster, config.keyspace, config.preparedStatementCacheSize)
      def this(naming: N, config: Config) = this(naming, CassandraContextConfig(config))
      def this(naming: N, configPrefix: String) = this(naming, LoadConfig(configPrefix))
    ```
      
    This is a good thing since we can pass (build) in special config direct onto driver, like pooling.       
    
    [Datastax Pooling](https://docs.datastax.com/en/developer/java-driver/2.1/manual/pooling/)
    
    A first try looks like this:
    
    ```
      val poolingOptions = new PoolingOptions
    
      val cluster = Cluster.builder
        .addContactPoint("127.0.0.1")
        .withPort(9042)
        .withPoolingOptions(poolingOptions)
        .build
    
      val db = new CassandraAsyncContext(SnakeCase, cluster, "db", 1000)
    ```
    
    This means that we could probably use the same options as supported in the [Cluster Builder](https://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/Cluster.Builder.html).
    
2. How can different versions of the same table be handled?
   
   The way we believe could be a viable option to handle different version of the same table can be found in the test _com.ubirch.QuillDifferentTableVersionsSpec_
   
   The general idea is:
   
   * There's a general case class that represents the table.
   
   ```
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
   ```  
   
   * We define a trait with one value that represents the table schema where we point at the table we need.
  
   ```
     trait TablePointer[T] {
       implicit val eventSchemaMeta: SchemaMeta[T]
     }
   ```
 
   * Then for every specific table (version), there's an object with its own queries, inserts, etc. that extends from the trait above mentioned
   
   ```
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
   ```    
   
3. What is the recommended way to manage the cassandra context throughout your models.?
    
    An interesting option is probably DI.
    
    Runtime Dependency Injection via Guice: See [Here](#guice-di-and-quill-context)
    
4. Does Quill take care of closing the connections or does an explicit close needs to be done?

### How to run

_Examples_

```
1. Start your Cassandra.
2. Run 'sbt'
3. Select project by running 'project quill' 
4. Run 'run' in your console
5. Select the example you think can be intersting.
```

_Tests_

You can run all tests by following the next instructions:

```
1. Start your Cassandra.
2. Run 'sbt'
3. Select project by running 'project quill'
4. Run 'sbt test'
```
  
Or you can run a specific test by doing:

```
1. Start your Cassandra.
2. Run 'sbt'
3. Select project by running 'project quill'
4. Run 'sbt testOnly TEST_CLASS'
```  

The available test classes are:

1. _com.ubirch.QuillOpsSpec_: Plays with basic algebraic operators.   
2. _com.ubirch.QuillSpec_: Something like above, but simpler.
3. _com.ubirch.QuillDifferentTableVersionsSpec_: Shows how to have different versions of the same table
4. _com.ubirch.QuillMirrorSpec_: Makes sure that select and insert queries use the correct table version and spits out the correct cql.
5. _com.ubirch.ClusterDISpec_: Quick test on how to use Guice to control the db connection.

## Phantom

**Page**: https://outworkers.github.io/phantom/

**Description**: Reactive type-safe Scala driver for Apache Cassandra/Datastax Enterprise

**Prerequisites:** 

* A running instance of [Apache Cassandra](http://cassandra.apache.org/)

**Notes**

* It supports explicit configs as per the options supported in the [Cluster Builder](https://docs.datastax.com/en/drivers/java/3.1/com/datastax/driver/core/Cluster.Builder.html).
* Table/Keyspace creation commands can be passed through to initialized session.
* UDT types are on the pro version only.
* Connectors configs [here](https://outworkers.github.io/phantom/basics/connectors.html).
* Customization of the schema names is also supported.
* The definition of the tables has to be explicitly done and special care should be taken into account when defining your column mapping, especially the partition and clustering keys.

https://github.com/outworkers/phantom/wiki/Indexes

### How to run

_Tests_

You can run all tests by following the next instructions:

```
1. Start your Cassandra.
2. Run 'sbt'
3. Select project by running 'project phantom'
4. Run 'sbt test'
```

## DB migrations management  
   
### How to run https://github.com/patka/cassandra-migration

_Tests_

You can run the tests by following the next instructions:

```
1. Start your Cassandra.
2. Run 'sbt'
3. Select project by running 'project migrationTools'
4. Run 'testOnly com.ubirch.CassandraMigration'
```

Table Output (Example):

![Schema Migration Table Example](https://raw.githubusercontent.com/ubirch/ubirch-cassandra-eval/master/readmeAssets/cassandra-migration-example.jpg "Schema Migration Table Example")

### How to run https://github.com/joeledwards/node-cassandra-migration

**Prerequisites:** 

* A running instance of [Apache Cassandra](http://cassandra.apache.org/)
* You have node and npm installed.
* You have run this on your cassandra db:

```
CREATE KEYSPACE node_cassandra_migration_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};
```

_Tests_

You can run the tests by following the next instructions:

Not installing tool globally:

```
1. cd into externalMigrationTools/node/node-cassandra-migration
2. npm install
3. ./node_modules/cassandra-migration/bin/cassandra-migration migration.json
```

Table Output:

![Schema Version Table Example](https://raw.githubusercontent.com/ubirch/ubirch-cassandra-eval/master/readmeAssets/node-cassandra-schema-version.jpg "Schema Version Table Example")


### How to run https://github.com/o19s/trireme/

**Prerequisites:** 

* A running instance of [Apache Cassandra](http://cassandra.apache.org/)
* Cqsl should also be installed
* python pip: 'sudo apt install python-pip'
* setuptools: 'pip install setuptools'
* Library itself: 'pip install trireme'
* Don't forget to add 'export PATH=~/.local/bin:$PATH' to your PATH
* Python invoke: 'sudo apt install python-invoke'

_Tests_

You can run the tests by following the next instructions:

**Notes**
 
 * For some reason, when following the instructions on its page, the commands that are issued fail with a 'did not receive all required positional arguments!'. 
 The way I managed to work around was through inspecting the command with the help command like this ' inv --help trireme.setup' and realizing that a '-c' was missing. 
 But I'm not sure what this flag does. I just added a dummy value and seemed to have work, e.g: inv trireme.setup -c db_test.
  
 * It's a bit cumbersome to work with, but once you get it, you can get through well.
 * After running migrate command, it creates a schema.cql in the db folder.
 * The configuration for solr seems to be mandatory, removing its config key, throws an error.
 * The timestamp naming seems very nice, very rails-like.
 * You don't have to have created the keyspace in advance. The create commands takes care of it.

Checkout Cassandra commands [here](https://github.com/o19s/trireme/#cassandra). 

```
1. cd into externalMigrationTools/python/trireme
2. inv trireme.cassandra.create -c db_test
3. inv trireme.cassandra.migrate -c db_test
4. check the migration table.
5. * You may to run 'inv trireme.cassandra.drop -c db_test'

* It might time out, I guess this is something that can be setup in cqsl.
```

Table Output:

![Migration Table Example](https://raw.githubusercontent.com/ubirch/ubirch-cassandra-eval/master/readmeAssets/trireme_migration_table_example.jpg "Migration Table Example")


### How to run https://github.com/Cobliteam/cassandra-migrate

**Prerequisites:** 

* A running instance of [Apache Cassandra](http://cassandra.apache.org/)
* python pip: 'sudo apt install python-pip'
* Don't forget to add 'export PATH=~/.local/bin:$PATH' to your PATH
* setuptools: 'pip install setuptools'
* Library itself: 'pip install cassandra-migrate'
* You have run the following script on you db:

```
 CREATE KEYSPACE python_cassandra_migrate_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};
```

_Tests_

You can run the tests by following the next instructions:

**Notes**
  
```
1. cd into externalMigrationTools/python/cassandraMigrate
2. cassandra-migrate -H 127.0.0.1 -p 9042 status
3. cassandra-migrate -H 127.0.0.1 -p 9042 migrate
4. cassandra-migrate -H 127.0.0.1 -p 9042 status

If the db already has stuff in it.
You should probably baseline it before running new migrations.

cassandra-migrate -H 127.0.0.1 -p 9042 baseline
```

Table Output:

![Database Migration Table Example](https://raw.githubusercontent.com/ubirch/ubirch-cassandra-eval/master/readmeAssets/cassandra-migrate-database-migrations-example.jpg "Database Migrations Table Example")

## Guice-DI and Quill Context

**Description**: Assembles a Quill set of queries based on Guice, maintaining one single instance of the db connection and with shutdown hooks.

**Prerequisites:** 

Same as in Quill. See [here](#quill).

### How to run

_Examples_

You can run the examples by following these instructions:

```
1. Start your Cassandra.
2. Run 'sbt'
3. Select project by running 'project di'
4. Run 'run'
5. You will see the available options. Type the number you would like to see
```  

* There are these examples:

_com.ubirch.guice.run_

_Test_

You can run all tests by following the next instructions:

```
1. Start your Cassandra.
2. Run 'sbt'
3. Select project by running 'project di'
4. Run 'sbt testOnly com.ubirch.GuiceConnectionServiceSpec'
```  

**Notes** 

* At first glance all seem to work very well, even evicted warning for having used most recent Guice. 
  The datastax driver has a dependency on an older Guice version.
  
* Services can be injected to parts of the code where needed in a very graceful manner.

  Check the guice folder to check the available services, namely:
  
  * ClusterService
  * ConnectionService
  * ConfigProvider
  * ExecutionProvider
  * Lifecyle

* Maybe a but is that as Guice is a runtime DI, and the compiled queries can't be translated into CQL until all is wired.

* Here's an example: 

```
trait ClusterConfigs {
  val contactPoints: List[String]
  val port: Int
}

trait ClusterService extends ClusterConfigs {
  val poolingOptions: PoolingOptions
  val cluster: Cluster
}

@Singleton
class DefaultClusterService @Inject() (config: Config) extends ClusterService {

  val contactPoints: List[String] = config.getStringList("eventLog.cluster.contactPoints").asScala.toList
  val port: Int = config.getInt("eventLog.cluster.port")

  val poolingOptions = new PoolingOptions

  override val cluster = Cluster.builder
    .addContactPoints(contactPoints: _*)
    .withPort(port)
    .withPoolingOptions(poolingOptions)
    .build

}

trait ConnectionServiceConfig {
  val keyspace: String
  val preparedStatementCacheSize: Int
}

trait ConnectionService extends ConnectionServiceConfig {
  type N <: NamingStrategy
  val context: CassandraAsyncContext[N]

}

@Singleton
class DefaultConnectionService @Inject() (clusterService: ClusterService, config: Config, lifecycle: Lifecycle)
  extends ConnectionService {

  val keyspace: String = config.getString("eventLog.cluster.keyspace")
  val preparedStatementCacheSize: Int = config.getInt("eventLog.cluster.preparedStatementCacheSize")

  type N = SnakeCase.type

  override val context =
    new CassandraAsyncContext(
      SnakeCase,
      clusterService.cluster,
      keyspace,
      preparedStatementCacheSize)

  lifecycle.addStopHook { () =>
    Future.successful(context.close())
  }

}

trait EventsByCatQueries extends TablePointer[Event] {

  import db._

  implicit val eventSchemaMeta = schemaMeta[Event]("events_by_cat")

  //These represent query descriptions only

  def selectAllQ = quote(query[Event])

  def byCatAndEventSourceAndYearAndMonthQ(category: String, eventSourceService: String, date: DateTime) = quote {
    query[Event]
      .filter(_.category == lift(category))
      .filter(_.eventSourceService == lift(eventSourceService))
      .filter(_.year == lift(date.year().get()))
      .filter(_.month == lift(date.monthOfYear().get()))
  }

  def byCatAndEventSourceAndYearAndMonthAndDayQ(category: String, eventSourceService: String, date: DateTime) = quote {
    query[Event]
      .filter(_.category == lift(category))
      .filter(_.eventSourceService == lift(eventSourceService))
      .filter(_.year == lift(date.year().get()))
      .filter(_.month == lift(date.monthOfYear().get()))
      .filter(_.day == lift(date.dayOfMonth().get()))
  }

  def byCatAndEventSourceAndYearAndMonthAndDayAndDeviceIdQ(category: String, eventSourceService: String, date: DateTime, deviceId: UUID) = quote {
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

@Singleton
class EventsByCat @Inject() (val connectionService: ConnectionService)(implicit ec: ExecutionContext) extends EventsByCatQueries {

  val db = connectionService.context

  import db._

  //These actually run the queries.

  def selectAll = run(selectAllQ)

  def byCatAndEventSourceAndYearAndMonth(category: String, eventSourceService: String, date: DateTime) =
    run(byCatAndEventSourceAndYearAndMonthQ(category, eventSourceService, date))

  def byCatAndEventSourceAndYearAndMonthAndDay(category: String, eventSourceService: String, date: DateTime) =
    run(byCatAndEventSourceAndYearAndMonthAndDayQ(category, eventSourceService, date))

  def byCatAndEventSourceAndYearAndMonthAndDayAndDeviceId(category: String, eventSourceService: String, date: DateTime, deviceId: UUID) =
    run(byCatAndEventSourceAndYearAndMonthAndDayAndDeviceIdQ(category, eventSourceService, date, deviceId))

}
```  

## MacWire DI and Quill Context

**Description**: Assembles a Quill set of queries based on MacWire and Traits, maintaining one single instance of the db connection and with shutdown hooks.

**Prerequisites:** 

Same as in Quill. See [here](#quill).

### How to run

_Examples_

You can run the examples by following these instructions:

```
1. Start your Cassandra.
2. Run 'sbt'
3. Select project by running 'project di'
4. Run 'run'
5. You will see the available options. Type the number you would like to see
```  

* There are these examples:

_com.ubirch.macwire.ImportedModule_

_Test_

You can run all tests by following the next instructions:

```
1. Start your Cassandra.
2. Run 'sbt'
3. Select project by running 'project di'
4. Run 'sbt testOnly com.ubirch.MacwireConnectionServiceSpec'
```  


### Changing Cassandra with ScyllaDB

By using ScyllaDB instead of Cassandra to run the tests, it was observed that
all tests passed well.

The only minor issue was when running the evolutions script on the db with
[Cassandra Migrate](https://github.com/Cobliteam/cassandra-migrate), as it threw a warning
about not supporting Light Weight Transactions.