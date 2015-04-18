name := "dashboard-ui"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.2"

resolvers += "Sage Repository" at "http://sagebionetworks.artifactoryonline.com/sagebionetworks/libs-releases-local"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.sagebionetworks" % "dashboard" % "0.7.1+",
  "org.springframework" % "spring-context" % "4.0.9.RELEASE",
  "org.springframework.data" % "spring-data-redis" % "1.4.2.RELEASE",
  "redis.clients" % "jedis" % "2.5.2",
  "joda-time" % "joda-time" % "2.7",
  "net.sf.opencsv" % "opencsv" % "2.3",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.9.29"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
