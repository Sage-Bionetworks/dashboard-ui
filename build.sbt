name := "dashboard-ui"

version := "1.0-SNAPSHOT"

resolvers += "Sage Repository" at "http://sagebionetworks.artifactoryonline.com/sagebionetworks/libs-releases-local"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "dashboard" % "dashboard" % "0.5.+",
  "org.springframework" % "spring-context" % "3.2.8.RELEASE",
  "org.springframework" % "spring-jdbc" % "3.2.8.RELEASE",
  "org.apache.commons" % "commons-dbcp2" % "2.0",
  "org.postgresql" % "postgresql" % "9.3-1101-jdbc41",
  "org.springframework.data" % "spring-data-redis" % "1.2.1.RELEASE",
  "redis.clients" % "jedis" % "2.4.1",
  "joda-time" % "joda-time" % "2.3",
  "net.sf.opencsv" % "opencsv" % "2.3",
  "com.amazonaws" % "aws-java-sdk" % "1.7.3",
  "org.openid4java" % "openid4java" % "0.9.8"
)

play.Project.playScalaSettings

scalacOptions ++= Seq("-feature")
