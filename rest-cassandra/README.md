
There are a lot of integration integration tools in scala ecosystems 

So why Quill ? 


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
 

## Quill

**Page**: http://getquill.io/

**Description**: Free/Libre Compile-time Language Integrated Queries for Scala

**Prerequisites:** 

* A running instance of [Apache Cassandra](http://cassandra.apache.org/)
* Make sure you have run this on your db.



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
   
 
 

   
### How to run 

```
1. Start your Cassandra.
2. Run 'sbt'
3. bla bla 
...
... 
... . Enjoy 

```

Table Output (Example):

![Schema Market Data Table Example](http://prntscr.com/nhzxl8 "Schema Market Data Table Example")
