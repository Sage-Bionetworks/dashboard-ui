name := "dashboard-ui"

version := "1.0-SNAPSHOT"

resolvers += "Sage Repository" at "http://sagebionetworks.artifactoryonline.com/sagebionetworks/libs-releases-local"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.sagebionetworks" % "dashboard" % "0.5.26+",
  "org.springframework" % "spring-context" % "3.2.10.RELEASE",
  "org.springframework" % "spring-jdbc" % "3.2.10.RELEASE",
  "org.apache.commons" % "commons-dbcp2" % "2.0.1",
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  "org.springframework.data" % "spring-data-redis" % "1.3.2.RELEASE",
  "redis.clients" % "jedis" % "2.4.1",
  "joda-time" % "joda-time" % "2.4",
  "net.sf.opencsv" % "opencsv" % "2.3",
  "com.amazonaws" % "aws-java-sdk" % "1.8.8",
  "org.openid4java" % "openid4java" % "0.9.8",
  "org.jasypt" % "jasypt" % "1.9.2"
)

play.Project.playScalaSettings

scalacOptions ++= Seq("-feature")
