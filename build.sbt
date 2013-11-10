name := "dashboard-ui"

version := "1.0-SNAPSHOT"

resolvers += "Sage Repository" at "http://sagebionetworks.artifactoryonline.com/sagebionetworks/libs-releases-local"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "dashboard" % "dashboard" % "0.1.7",
  "org.springframework" % "spring-context" % "3.1.4.RELEASE",
  "org.springframework.data" % "spring-data-redis" % "1.1.0.RELEASE",
  "redis.clients" % "jedis" % "2.1.0",
  "joda-time" % "joda-time" % "2.3"
)     

play.Project.playScalaSettings

