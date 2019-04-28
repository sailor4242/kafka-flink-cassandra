import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.dockerBaseImage
import sbt.addCompilerPlugin
import sbt.Keys._

name := "kafka-flink-cassandra"

version := "0.0.1-SANPSHOT"

lazy val root = (
  project.in(file("."))
    aggregate(websocketServer, websocketClientKafka, flinkProcessor, userRestServer,
    restCassandra, sparkProcessor2)
  )

lazy val metaParadiseVersion = "3.0.0-M11"
lazy val catsMTLVersion = "0.4.0"
lazy val catsVersion = "1.4.0"
lazy val log4CatsVersion = "0.2.0"
lazy val circeDerivationVersion = "0.11.0-M1"
lazy val circeVersion = "0.11.1"
lazy val pureConfigVersion = "0.10.0"
lazy val enumeratumVersion = "1.5.13"
lazy val ScalaZVersion = "7.3.0-M28"
lazy val ZIOVersion = "0.18"
lazy val doobieVersion = "0.7.0-M3"
lazy val flinkVersion = "1.7.2"

lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4"
lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.4"

lazy val sparkVersion = "2.4.1"
// lazy val catsVersion = "1.0.1"
lazy val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
lazy val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion
lazy val kafkaStreaming = "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion
lazy val kafkaSql = "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion
lazy val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion
lazy val typeSafeConfig = "com.typesafe" % "config" % "1.3.2"
lazy val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
lazy val sparkTests = "com.holdenkarau" %% "spark-testing-base" % "2.2.0_0.8.0"
lazy val cats = "org.typelevel" %% "cats-core" % catsVersion
lazy val catsLaws = "org.typelevel" %% "cats-laws" % catsVersion
lazy val catsTests = "org.typelevel" %% "cats-testkit" % catsVersion

lazy val websocketServer = (project in file("websocket-server"))
  .dependsOn(model)
  .settings(
    // baseSettings,
    baseSettingsScala211,
    compilerPlugins,
    libraryDependencies ++= Seq(
      "io.chrisdavenport" %% "log4cats-core" % log4CatsVersion,
      "io.chrisdavenport" %% "log4cats-slf4j" % log4CatsVersion,
      "io.chrisdavenport" %% "cats-par" % "0.2.0",
      "io.monix" %% "monix" % "3.0.0-RC2"
    ) ++ commonDependencies ++ akka,
    mainClass in Compile := Some("App"),
    packageName in Docker := "websocket-server",
    dockerBaseImage := "openjdk:8",
    dockerUpdateLatest := true
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)

lazy val websocketClientKafka = (project in file("websocket-client-kafka"))
  .dependsOn(model)
  .settings(
    // baseSettings,
    baseSettingsScala211,
    compilerPlugins,
    libraryDependencies ++= commonDependencies ++ akka,
    mainClass in Compile := Some("WebSocketClientToKafka"),
    packageName in Docker := "websocket-client",
    dockerBaseImage := "openjdk:8",
    dockerUpdateLatest := true
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)

lazy val restCassandra = (project in file("rest-cassandra"))
  .dependsOn(model)
  .settings(
    // baseSettings,
    baseSettingsScala211,
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4"),
    libraryDependencies ++= Seq(
      "io.chrisdavenport" %% "log4cats-slf4j" % log4CatsVersion,
      "io.chrisdavenport" %% "cats-par" % "0.2.0",
      "io.monix" %% "monix" % "3.0.0-RC2",
      "com.typesafe" % "config" % "1.3.2",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8",
      "com.typesafe.akka" %% "akka-stream" % "2.5.12",
      "com.typesafe.akka" %% "akka-http" % "10.1.8",
      "com.typesafe.akka" %% "akka-http-core" % "10.1.8",
      "io.getquill" %% "quill-cassandra-monix" % "3.1.0",
      "de.heikoseeberger" %% "akka-http-circe" % "1.24.3",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-derivation" % circeDerivationVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-java8" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "com.beachape" %% "enumeratum" % enumeratumVersion,
      "com.beachape" %% "enumeratum-circe" % enumeratumVersion,
      "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ),
    mainClass in Compile := Some("QuillQueriesToRestApi")
  )

lazy val sparkProcessor2 = (project in file("spark-processor2"))
  //.dependsOn(model)
  .settings(
    baseSettingsScala211,
    libraryDependencies ++= Seq(
      sparkCore/* % Provided*/,
      sparkStreaming/* % Provided*/,
      sparkSql/* % Provided*/,
      kafkaStreaming,
      kafkaSql,
      typeSafeConfig,
      "com.datastax.cassandra"  % "cassandra-driver-core"         % "3.6.0",
      "com.datastax.spark"      %% "spark-cassandra-connector" % "2.4.1",
      cats,
      logging,
      scalaTest % Test,
      scalaCheck % Test,
      catsLaws % Test,
      catsTests % Test,
       sparkTests % Test,
      "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
    ),
    mainClass in Compile := Some("SparkAppRunner")
  )

lazy val flinkProcessor = (project in file("flink-processor"))
  .dependsOn(model)
  .settings(
    // baseSettings,
    baseSettingsScala211,
    compilerPlugins,
    libraryDependencies ++= commonDependencies ++ flink,
    mainClass in Compile := Some("FlinkProcessTopic"),
    packageName in Docker := "flink-processor",
    dockerBaseImage := "openjdk:8",
    dockerUpdateLatest := true
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)

lazy val userRestServer = (project in file("user-rest-server"))
  //.dependsOn(model)
  .settings(
    // baseSettings,
  baseSettingsScala211,
    compilerPlugins,
    libraryDependencies ++= commonDependencies ++ akka ++ doobie ++ scalaz,
    mainClass in Compile := Some("UserRestServer"),
    packageName in Docker := "user-rest-server",
    dockerBaseImage := "openjdk:8",
    dockerExposedPorts ++= Seq(8081),
    dockerUpdateLatest := true
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)

lazy val model = (project in file("model"))
  .settings(
    // baseSettings,
    baseSettingsScala211,
    libraryDependencies ++= circe,
    libraryDependencies ++= Seq(
      "com.beachape" %% "enumeratum" % enumeratumVersion,
      "com.beachape" %% "enumeratum-circe" % enumeratumVersion,
    ),
    addCompilerPlugin("org.scalameta" % "paradise" % metaParadiseVersion cross CrossVersion.full),
    scalacOptions += "-Xplugin-require:macroparadise",
    mainClass in Compile := Some("model")
  )

lazy val commonDependencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
  "com.typesafe" % "config" % "1.3.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

lazy val doobie = Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.tpolecat" %% "doobie-h2" % doobieVersion,
  "org.flywaydb" % "flyway-core" % "5.2.4",
  "com.h2database" % "h2" % "1.4.199"
)

lazy val flink = Seq(
  "org.apache.flink" %% "flink-scala",
  "org.apache.flink" %% "flink-streaming-scala",
  "org.apache.flink" %% "flink-connector-kafka-0.11",
  "org.apache.flink" %% "flink-connector-cassandra"
).map(_ % flinkVersion)

lazy val akka = Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.22",
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "com.typesafe.akka" %% "akka-http-core" % "10.1.1",
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.0.1",
  "de.heikoseeberger" %% "akka-http-circe" % "1.25.2"
)

lazy val scalaz = Seq(
  "org.scalaz" %% "scalaz-core" % ScalaZVersion,
  "org.scalaz" %% "scalaz-zio" % ZIOVersion,
  "org.scalaz" %% "scalaz-zio-interop-cats" % ZIOVersion
)

lazy val circe = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-java8"
).map(_ % circeVersion) :+ "io.circe" %% "circe-derivation" % circeDerivationVersion

lazy val baseSettings = Seq(
  scalaVersion in ThisBuild := "2.12.7",
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.bintrayRepo("ovotech", "maven")
  ),
  scalacOptions ++= commonScalacOptions,
  scalacOptions ++= Seq("-Xmax-classfile-name", "128"),
  parallelExecution in Test := false,
  sources in(Compile, doc) := Nil,
  publishTo := None,
  cancelable in Global := true
)

lazy val baseSettingsScala211 = Seq(
  scalaVersion in ThisBuild := "2.11.12",
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.bintrayRepo("ovotech", "maven")
  ),
  scalacOptions ++= commonScalacOptions,
  scalacOptions ++= Seq("-Xmax-classfile-name", "128"),
  parallelExecution in Test := false,
  sources in(Compile, doc) := Nil,
  publishTo := None,
  cancelable in Global := true
)

lazy val compilerPlugins = Seq(
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9"),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")
)

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ypartial-unification"
)