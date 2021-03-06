import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.dockerBaseImage
import sbt.addCompilerPlugin
import sbt.Keys._

name := "kafka-flink-cassandra"

version := "0.0.1-SANPSHOT"

lazy val root = (
  project.in(file("."))
    aggregate(websocketServer, websocketClientKafka, flinkProcessor, userRestServer)
  )

lazy val metaParadiseVersion = "3.0.0-M11"
lazy val catsMTLVersion = "0.4.0"
lazy val catsVersion = "1.6.0"
lazy val catsEffectVersion = "1.2.0"
lazy val log4CatsVersion = "0.2.0"
lazy val circeDerivationVersion = "0.11.0-M1"
lazy val circeVersion = "0.11.1"
lazy val pureConfigVersion = "0.10.0"
lazy val enumeratumVersion = "1.5.13"
lazy val ScalaZVersion = "7.3.0-M28"
lazy val ZIOVersion = "0.18"
lazy val doobieVersion = "0.7.0-M3"
lazy val flinkVersion = "1.7.2"

lazy val websocketServer = (project in file("websocket-server"))
  .dependsOn(model)
  .settings(
    baseSettings,
    compilerPlugins,
    libraryDependencies ++= cats ++ commonDependencies ++ akka,
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
    baseSettings,
    compilerPlugins,
    libraryDependencies ++= commonDependencies ++ akka,
    mainClass in Compile := Some("App"),
    packageName in Docker := "websocket-client",
    dockerBaseImage := "openjdk:8",
    dockerUpdateLatest := true
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)

lazy val flinkProcessor = (project in file("flink-processor"))
  .dependsOn(model)
  .settings(
    baseSettings,
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
  .dependsOn(model)
  .settings(
    baseSettings,
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
    baseSettings,
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

lazy val cats = Seq(
  "org.typelevel" %% "cats-core"   % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion
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

lazy val compilerPlugins = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.0"),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0-M4")
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
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ypartial-unification"
)