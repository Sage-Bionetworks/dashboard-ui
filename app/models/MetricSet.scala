package models

import scala.language.postfixOps
import scala.collection.JavaConversions.asScalaBuffer
import org.joda.time.DateTime
import org.sagebionetworks.dashboard.model.{ Interval, Statistic, CountDataPoint }
import org.sagebionetworks.dashboard.service.{ CountDataPointConverter, EntityIdToName, UserIdToName }
import org.sagebionetworks.dashboard.service.MetricReader
import org.sagebionetworks.dashboard.util.TimeDataPointUtil
import context.SpringContext
import MetricType._

/**
 * One set of metrics. This is a template to query a set of metrics and to assemble a data set.
 *
 * @param name         The descriptive name of this metric set
 * @param description  Short description of this metric set
 * @param interval     Default aggregation interval
 * @param statistic    Optional default statistic
 * @param start        Offset in days of the starting time
 * @param end          Offset in days of the ending time
 * @param dataSet      Function to generate the data set for this metric set
 */
case class Metric(
  name: String,
  description: String,
  start: Int,
  end: Int,
  interval: Interval,
  statistic: Statistic,
  dataSet: (DateTime, DateTime, Interval, Statistic, Int) => DataSet)

object MetricSet {

  private val metricReader = SpringContext.getBean(classOf[MetricReader])
  private val entityIdToName = SpringContext.getBean(classOf[EntityIdToName])
  private val userIdToName = SpringContext.getBean(classOf[UserIdToName])

  val metricMap = Map(

    "Users" -> collection.immutable.ListMap(

      MetricHandle(ActiveUser, "user") -> Metric(
        name = "Count of Active Users",
        description = "The number of active users who have used Synapse at least 3 days per month.",
        start = 210,
        end = 0,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val data = metricReader.getUniqueCount("activeUser", interval, start, end, 3L, Long.MaxValue);
          DataSet(
            xLabel = Some("date"),
            yLabel = Some("count of active users"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(data map (d => d.x) toList),
            yHeaders = List("all users"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Unique, "user") -> Metric(
        name = "Count of Unique Users",
        description = "The number of unique users during a period of time.",
        start = 7,
        end = 0,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val data = metricReader.getUniqueCount("uniqueUser", interval, start, end);
          DataSet(
            xLabel = Some("date"),
            yLabel = Some("count of unique users"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(data map (d => d.x) toList),
            yHeaders = List("all users"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Top, "user") -> Metric(
        name = "Top Users (Session Count)",
        description = "Top 20 users who have registered the most activitities.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val baseUrl = "https://www.synapse.org/#!Profile:"
          val data = metricReader.getTop("uniqueUser", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
            xValues = List(
              data map (d => userIdToName.convert(d).x) toList,
              data map (d => d.x) toList,
              data map (d => baseUrl + d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(TopByDay, "user") -> Metric(
        name = "Top Users (Day Count)",
        description = "Top 20 users who have registered the most days.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val baseUrl = "https://www.synapse.org/#!Profile:"
          val data = metricReader.getTop("activeUser", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
            xValues = List(
              data map (d => userIdToName.convert(d).x) toList,
              data map (d => d.x) toList,
              data map (d => baseUrl + d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Trending, "user") -> Metric(
        name = "Trending Users (Session Count)",
        description = "List of the top 20 most active users.",
        start = 60,
        end = 1,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val topData = metricReader.getTop("uniqueUser", interval, end, 0, 10)
          val trendingData = topData map (d => {
            metricReader.getCount("uniqueUser", d.id, interval, start, end)
          })
          DataSet(
            xLabel = Some("Date & Time"),
            yLabel = Some("Session Count"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(trendingData(0) map (d => d.x) toList),
            yHeaders = topData map (d => userIdToName.convert(d).x) toList,
            yValues = trendingData map (series => {
              series map (d => d.y) toList
            }) toList)
        })),

    "Entities" -> collection.immutable.ListMap(

      MetricHandle(Top, "project") -> Metric(
        name = "Top Projects (Session Count)",
        description = "List of the top 20 most accessed projects.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val baseUrl = "https://www.synapse.org/#!Synapse:"
          val data = metricReader.getTop("topProject", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
            xValues = List(
              data map (d => entityIdToName.convert(d).x) toList,
              data map (d => d.x) toList,
              data map (d => baseUrl + d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Top, "project-day-count") -> Metric(
        name = "Top Projects (Day Count)",
        description = "List of the top 20 most accessed projects.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val baseUrl = "https://www.synapse.org/#!Synapse:"
          val data = metricReader.getTop("topProjectByDay", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
            xValues = List(
              data map (d => entityIdToName.convert(d).x) toList,
              data map (d => d.x) toList,
              data map (d => baseUrl + d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Trending, "project") -> Metric(
        name = "Trending Projects (Session Count)",
        description = "List of the top 10 most accessed projects.",
        start = 60,
        end = 1,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val topData = metricReader.getTop("topProject", interval, end, 0, 10)
          val trendingData = topData map (d => {
            metricReader.getCount("topProject", d.id, interval, start, end)
          })
          DataSet(
            xLabel = Some("Date & Time"),
            yLabel = Some("Session Count"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(trendingData(0) map (d => d.x) toList),
            yHeaders = topData map (d => d.id) toList,
            yValues = trendingData map (series => {
              series map (d => d.y) toList
            }) toList)
        }),

      MetricHandle(Top, "entity-read") -> Metric(
        name = "Top Entities (Session Count)",
        description = "List of the top 20 most accessed entities.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val baseUrl = "https://www.synapse.org/#!Synapse:"
          val data = metricReader.getTop("entityRead", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
            xValues = List(
              data map (d => entityIdToName.convert(d).x) toList,
              data map (d => d.x) toList,
              data map (d => baseUrl + d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Top, "entity-write") -> Metric(
        name = "Entities Created/Updated (Session Count)",
        description = "List of the top 20 most updated entities.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val baseUrl = "https://www.synapse.org/#!Synapse:"
          val data = metricReader.getTop("entityWrite", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
            xValues = List(
              data map (d => entityIdToName.convert(d).x) toList,
              data map (d => d.x) toList,
              data map (d => baseUrl + d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Trending, "entity-read") -> Metric(
        name = "Trending Entities (Session Count)",
        description = "List of the top 10 most accessed entities.",
        start = 60,
        end = 1,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val topData = metricReader.getTop("entityRead", interval, end, 0, 10)
          val trendingData = topData map (d => {
            metricReader.getCount("entityRead", d.id, interval, start, end)
          })
          DataSet(
            xLabel = Some("Date & Time"),
            yLabel = Some("Session Count"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(trendingData(0) map (d => d.x) toList),
            yHeaders = topData map (d => d.id) toList,
            yValues = trendingData map (series => {
              series map (d => d.y) toList
            }) toList)
        }),

      MetricHandle(Top, "fileDownload") -> Metric(
        name = "Top File Downloads (Session Count)",
        description = "List of the top 20 most downloaded files.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val baseUrl = "https://www.synapse.org/#!Synapse:"
          val data = metricReader.getTop("fileDownload", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
            xValues = List(
              data map (d => entityIdToName.convert(d).x) toList,
              data map (d => d.x) toList,
              data map (d => baseUrl + d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(TopByDay, "wiki-write-by-user") -> Metric(
        name = "Top Wiki Users (Creation, Day Count)",
        description = "List of the top 20 users who write Wiki pages the most.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val baseUrl = "https://www.synapse.org/#!Profile:"
          val data = metricReader.getTop("wikiWriteByUser", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
            xValues = List(
              data map (d => userIdToName.convert(d).x) toList,
              data map (d => d.x) toList,
              data map (d => baseUrl + d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(TopByDay, "wiki-read-by-user") -> Metric(
        name = "Top Wiki Users (Consumption, Day Count)",
        description = "List of the top 20 users who read Wiki pages the most.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val baseUrl = "https://www.synapse.org/#!Profile:"
          val data = metricReader.getTop("wikiReadByUser", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
            xValues = List(
              data map (d => userIdToName.convert(d).x) toList,
              data map (d => d.x) toList,
              data map (d => baseUrl + d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(TopByDay, "wiki-write-by-object") -> Metric(
        name = "Top Wiki Objects (Creation, Day Count)",
        description = "List of the top 20 wiki objects.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val baseUrl = "https://www.synapse.org/#!Synapse:"
          val data = metricReader.getTop("wikiWriteByObject", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
            xValues = List(
              data map (d => entityIdToName.convert(d).x) toList,
              data map (d => d.x) toList,
              data map (d => baseUrl + d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(TopByDay, "wiki-read-by-object") -> Metric(
        name = "Top Wiki Objects (Consumption, Day Count)",
        description = "List of the top 20 wiki objects.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val baseUrl = "https://www.synapse.org/#!Synapse:"
          val data = metricReader.getTop("wikiReadByObject", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
            xValues = List(
              data map (d => entityIdToName.convert(d).x) toList,
              data map (d => d.x) toList,
              data map (d => baseUrl + d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        })),

    "APIs & Clients" -> collection.immutable.ListMap(

      MetricHandle(Top, "method") -> Metric(
        name = "Top REST APIs (Session Count)",
        description = "List of the top 20 most accessed REST APIs.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val data = metricReader.getTop("topMethod", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name),
            xValues = List(
              data map (d => {
                // Pad the HTTP method name to align the URLs after it
                val parts = d.x.split(" ")
                parts(0).toUpperCase.padTo(5, ' ') + parts(1)
              }) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Top, "client") -> Metric(
        name = "Top Clients (Session Count)",
        description = "List of the top 20 clients.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val data = metricReader.getTop("topClient", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name),
            xValues = List(data map (d => d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Top, "python") -> Metric(
        name = "Top Python Clients",
        description = "List of the top 20 Python clients.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val data = metricReader.getTop("topPythonClient", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name),
            xValues = List(data map (d => d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Top, "r") -> Metric(
        name = "Top R Clients",
        description = "List of the top 20 R clients.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val data = metricReader.getTop("topRClient", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name),
            xValues = List(data map (d => d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Top, "web") -> Metric(
        name = "Top Web Clients",
        description = "List of the top 20 web clients.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val data = metricReader.getTop("topWebClient", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name),
            xValues = List(data map (d => d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        })),

    "Latencies & Errors" -> collection.immutable.ListMap(

      MetricHandle(Top, "status-code") -> Metric(
        name = "Top Status Codes (Session Count)",
        description = "List HTTP status codes ordered by session count.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val data = metricReader.getTop("statusCode", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name),
            xValues = List(data map (d => d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Top, "error-status-code") -> Metric(
        name = "Top Error Status Codes (Session Count)",
        description = "List HTTP error status codes ordered by session count.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val data = metricReader.getTop("errorStatusCode", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name),
            xValues = List(data map (d => d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Trending, "error-status-code") -> Metric(
        name = "Trending HTTP error status codes (Session Count)",
        description = "List of trending HTTP error status codes.",
        start = 60,
        end = 1,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val topData = metricReader.getTop("errorStatusCode", interval, end, 0, 10)
          val trendingData = topData map (d => {
            metricReader.getCount("errorStatusCode", d.id, interval, start, end)
          })
          DataSet(
            xLabel = Some("Date & Time"),
            yLabel = Some("Session Count"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(trendingData(0) map (d => d.x) toList),
            yHeaders = topData map (d => d.id) toList,
            yValues = trendingData map (series => {
              series map (d => d.y) toList
            }) toList)
        }),

      MetricHandle(Latency, "global") -> Metric(
        name = "Global Latencies",
        description = "Latency in milliseconds for all the REST APIs.",
        start = 7,
        end = 0,
        interval = Interval.hour,
        statistic = Statistic.avg,
        dataSet = (start, end, interval, statistic, page) => {
          val timeseries = metricReader.getTimeSeries("globalLatency", start, end,
            statistic, interval)
          DataSet(
            xLabel = Some("time"),
            yLabel = Some("latency (ms)"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(timeseries map (d => d.x) toList),
            yHeaders = List("ALL"),
            yValues = List(timeseries map (d => d.y) toList))
        }),

      MetricHandle(Latency, "postEntityHeader") -> Metric(
        name = "POST-Entity-Header Latencies",
        description = "Latency in milliseconds for the POST-entity-header REST API.",
        start = 7,
        end = 0,
        interval = Interval.hour,
        statistic = Statistic.avg,
        dataSet = (start, end, interval, statistic, page) => {
          val timeseries = metricReader.getTimeSeries("postEntityHeader", start, end,
            statistic, interval)
          DataSet(
            xLabel = Some("time"),
            yLabel = Some("latency (ms)"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(timeseries map (d => d.x) toList),
            yHeaders = List("PEH"),
            yValues = List(timeseries map (d => d.y) toList))
        }),

      MetricHandle(Latency, "getEntityBundle") -> Metric(
        name = "GET-Entity-Bundle Latencies",
        description = "Latency in milliseconds for the GET-entity-bundle REST API.",
        start = 7,
        end = 0,
        interval = Interval.hour,
        statistic = Statistic.avg,
        dataSet = (start, end, interval, statistic, page) => {
          val timeseries = metricReader.getTimeSeries("getEntityBundle", start, end,
            statistic, interval)
          DataSet(
            xLabel = Some("time"),
            yLabel = Some("latency (ms)"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(timeseries map (d => d.x) toList),
            yHeaders = List("GEB"),
            yValues = List(timeseries map (d => d.y) toList))
        }),

      MetricHandle(Latency, "query") -> Metric(
        name = "Query Latencies",
        description = "Latency in milliseconds for the query REST API.",
        start = 7,
        end = 0,
        interval = Interval.hour,
        statistic = Statistic.avg,
        dataSet = (start, end, interval, statistic, page) => {
          val timeseries = metricReader.getTimeSeries("query", start, end,
            statistic, interval)
          DataSet(
            xLabel = Some("time"),
            yLabel = Some("latency (ms)"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(timeseries map (d => d.x) toList),
            yHeaders = List("QRY"),
            yValues = List(timeseries map (d => d.y) toList))
        }),

      MetricHandle(Latency, "search") -> Metric(
        name = "Search Latencies",
        description = "Latency in milliseconds for the search REST API.",
        start = 7,
        end = 0,
        interval = Interval.hour,
        statistic = Statistic.avg,
        dataSet = (start, end, interval, statistic, page) => {
          val timeseries = metricReader.getTimeSeries("search", start, end,
            statistic, interval)
          DataSet(
            xLabel = Some("time"),
            yLabel = Some("latency (ms)"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(timeseries map (d => d.x) toList),
            yHeaders = List("SRCH"),
            yValues = List(timeseries map (d => d.y) toList))
        }),

      MetricHandle(Latency, "desc") -> Metric(
        name = "GET-Descendants Latencies",
        description = "Latency in milliseconds for the GET-descendants REST API.",
        start = 7,
        end = 0,
        interval = Interval.hour,
        statistic = Statistic.avg,
        dataSet = (start, end, interval, statistic, page) => {
          val timeseries = metricReader.getTimeSeries("getDescendants", start, end,
            statistic, interval)
          DataSet(
            xLabel = Some("time"),
            yLabel = Some("latency (ms)"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(timeseries map (d => d.x) toList),
            yHeaders = List("DESC"),
            yValues = List(timeseries map (d => d.y) toList))
        })),

    "Certified Users" -> collection.immutable.ListMap(

      MetricHandle(Unique, "certifiedUserQuizRequest") -> Metric(
        name = "Quiz Requests vs Quiz Submissions (Unique Count)",
        description = "The number of unique requests vs the number of unique submissions during a period of time.",
        start = 7,
        end = 0,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val rqData = metricReader.getUniqueCount("certifiedUserQuizRequest", interval, start, end);
          val smData = metricReader.getUniqueCount("certifiedUserQuizSubmit", interval, start, end);
          val map = TimeDataPointUtil.createMergeMap(
                java.util.Arrays.asList(rqData, smData), 2);
          val tslist = TimeDataPointUtil.getMergeTimeStampList(map);

          DataSet(
            xLabel = Some("date"),
            yLabel = Some("unique requests / unique submissions"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(tslist toList),
            yHeaders = List("all requests", "all submissions"),
            yValues = List(TimeDataPointUtil.getMergeValueList(tslist, map, 0) toList,
                           TimeDataPointUtil.getMergeValueList(tslist, map, 1) toList))
        })),

    "File Download Report" -> collection.immutable.ListMap(

      MetricHandle(Report, "fileDownloadReport") -> Metric(
        name = "File Download Report",
        description = "The list of users who downloaded a specific file.",
        start = 1,
        end = 1,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page) => {
          val baseUrl = "https://www.synapse.org/#!Profile:"
          val data = metricReader.getFileDownloadReport("fileDownloadReport", "entityId", start, interval);

          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL, DataHeader.Timestamp, DataHeader.Client),
            xValues = List(
                data map (d => userIdToName.convert(new CountDataPoint(d.userId, 0)).x) toList,
                data map (d => d.userId) toList,
                data map (d => baseUrl + d.userId) toList,
                data map (d => d.timestamp) toList,
                data map (d => d.client) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => "1") toList))
        })))

  def findMetric(handle: MetricHandle) = {
    metricMap map (subMap => { subMap._2 get handle }) find (metric => metric != None) get
  }
}
