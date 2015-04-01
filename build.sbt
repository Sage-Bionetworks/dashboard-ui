name := "dashboard-ui"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.2"

resolvers += "Sage Repository" at "http://sagebionetworks.artifactoryonline.com/sagebionetworks/libs-releases-local"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.sagebionetworks" % "dashboard" % "0.7.0+",
  "org.springframework" % "spring-context" % "3.2.10.RELEASE",
  "org.springframework" % "spring-jdbc" % "3.2.10.RELEASE",
  "org.apache.commons" % "commons-dbcp2" % "2.0.1",
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  "org.springframework.data" % "spring-data-redis" % "1.3.4.RELEASE",
  "redis.clients" % "jedis" % "2.4.1",
  "joda-time" % "joda-time" % "2.5",
  "net.sf.opencsv" % "opencsv" % "2.3",
  "com.amazonaws" % "aws-java-sdk" % "1.8.11",
  "org.openid4java" % "openid4java" % "0.9.8"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

