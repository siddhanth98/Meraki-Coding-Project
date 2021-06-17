lazy val logbackCore = "ch.qos.logback" % "logback-core" % "1.2.3"
lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"
lazy val slf4j = "org.slf4j" %"slf4j-api" %"1.7.30" % "test"

lazy val typesafe = "com.typesafe" % "config" % "1.4.0"
lazy val junit = "junit" % "junit" % "4.9" % Test
lazy val junitInterface = "com.novocode" % "junit-interface" % "0.11" % Test
lazy val jdbc = "org.postgresql" % "postgresql" % "42.2.21"
lazy val http = "com.sun.net.httpserver" % "http" % "20070405"
lazy val jackson = "com.fasterxml.jackson.core" % "jackson-databind" % "2.12.1"
lazy val httpClient = "org.apache.httpcomponents.client5" % "httpclient5" % "5.1"

libraryDependencies ++=
  Seq(logbackCore, logbackClassic, slf4j,
    typesafe, junit, junitInterface, jdbc,
    http, jackson, httpClient)