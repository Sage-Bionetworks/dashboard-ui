name := "dashboard-ui"

version := "1.0-SNAPSHOT"

resolvers += "Sage Repository" at "http://sagebionetworks.artifactoryonline.com/sagebionetworks/libs-releases-local"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "dashboard" % "dashboard" % "0.4.+",
  "org.springframework" % "spring-context" % "3.1.4.RELEASE",
  "org.springframework.data" % "spring-data-redis" % "1.1.0.RELEASE",
  "redis.clients" % "jedis" % "2.1.0",
  "joda-time" % "joda-time" % "2.3",
  "net.sf.opencsv" % "opencsv" % "2.3",
  "com.amazonaws" % "aws-java-sdk" % "1.6.11",
  "org.openid4java" % "openid4java" % "0.9.8"
)

play.Project.playScalaSettings
