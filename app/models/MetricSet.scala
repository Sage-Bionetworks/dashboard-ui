package models

import scala.language.postfixOps
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConverters._
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
  dataSet: (DateTime, DateTime, Interval, Statistic, Int, String) => DataSet)

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
        dataSet = (start, end, interval, statistic, page, text) => {
          val data = metricReader.getUniqueCount("activeUser", interval, start, end, 3L, Long.MaxValue)
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
        dataSet = (start, end, interval, statistic, page, text) => {
          val data = metricReader.getUniqueCount("uniqueUser", interval, start, end)
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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

      MetricHandle(Top, "entity-read") -> Metric(
        name = "Top Entities (Session Count)",
        description = "List of the top 20 most accessed entities.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
          val data = metricReader.getTop("errorStatusCode", interval, start, page * 20, (page + 1) * 20)
          DataSet(
            xLabel = None,
            yLabel = None,
            xHeaders = List(DataHeader.Name),
            xValues = List(data map (d => d.x) toList),
            yHeaders = List("count"),
            yValues = List(data map (d => d.y) toList))
        }),

      MetricHandle(Trending, "status-code") -> Metric(
        name = "Trending HTTP status codes (Percentage)",
        description = "List of trending HTTP status codes.",
        start = 60,
        end = 1,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val topData = metricReader.getTop("statusCode", interval, end, 0, 10)
          val trendingData = topData map (d => {
            metricReader.getCount("statusCode", d.id, interval, start, end)
          })
          val map = TimeDataPointUtil.createMergeMap(trendingData asJava, trendingData size)
          val tslist = TimeDataPointUtil.getMergeTimeStampList(map)
          DataSet(
            xLabel = Some("Date & Time"),
            yLabel = Some("Percentage"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(tslist toList),
            yHeaders = topData map (d => d.id) toList,
            yValues = (0 until (trendingData size)) map (d => 
              TimeDataPointUtil.getPercentageList(tslist, map, d) toList) toList)
        }),

      MetricHandle(Trending, "error-status-code") -> Metric(
        name = "Trending HTTP error status codes (Session Count)",
        description = "List of trending HTTP error status codes.",
        start = 60,
        end = 1,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
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
        name = "Global Latencies and Query Latencies",
        description = "The latency in milliseconds for all the REST APIs and the latency in milliseconds for the query REST API.",
        start = 7,
        end = 0,
        interval = Interval.hour,
        statistic = Statistic.avg,
        dataSet = (start, end, interval, statistic, page, text) => {
          val gtimeseries = metricReader.getTimeSeries("globalLatency", start, end,
            statistic, interval);
          val qtimeseries = metricReader.getTimeSeries("query", start, end,
            statistic, interval);
          val map = TimeDataPointUtil.createMergeMap(java.util.Arrays.asList(gtimeseries, qtimeseries), 2);
          val tslist = TimeDataPointUtil.getMergeTimeStampList(map);
          DataSet(
            xLabel = Some("time"),
            yLabel = Some("latency (ms)"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(tslist toList),
            yHeaders = List("Global", "Query"),
            yValues = List(TimeDataPointUtil.getMergeValueList(tslist, map, 0) toList,
                           TimeDataPointUtil.getMergeValueList(tslist, map, 1) toList))
        }),

      MetricHandle(Latency, "postEntityHeader") -> Metric(
        name = "POST-Entity-Header Latencies",
        description = "Latency in milliseconds for the POST-entity-header REST API.",
        start = 7,
        end = 0,
        interval = Interval.hour,
        statistic = Statistic.avg,
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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

      MetricHandle(Latency, "search") -> Metric(
        name = "Search Latencies",
        description = "Latency in milliseconds for the search REST API.",
        start = 7,
        end = 0,
        interval = Interval.hour,
        statistic = Statistic.avg,
        dataSet = (start, end, interval, statistic, page, text) => {
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
        dataSet = (start, end, interval, statistic, page, text) => {
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
        name = "Number of Requests, Submissions, and Certified Users",
        description = "The number of unique users making a request, submitting a quiz, and passing the quiz during a period of time.",
        start = 7,
        end = 0,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val rqData = metricReader.getUniqueCount("certifiedUserQuizRequest", interval, start, end)
          val smData = metricReader.getUniqueCount("certifiedUserQuizSubmit", interval, start, end)
          val cuData = metricReader.getUniqueCount("certifiedUser", interval, start, end)
          val map = TimeDataPointUtil.createMergeMap(
                java.util.Arrays.asList(rqData, smData, cuData), 3)
          val tslist = TimeDataPointUtil.getMergeTimeStampList(map)

          DataSet(
            xLabel = Some("date"),
            yLabel = Some("unique users making a request, unique user making a submission, unique certified users"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(tslist toList),
            yHeaders = List("all requests", "all submissions", "certified users"),
            yValues = List(TimeDataPointUtil.getMergeValueList(tslist, map, 0) toList,
                           TimeDataPointUtil.getMergeValueList(tslist, map, 1) toList,
                           TimeDataPointUtil.getMergeValueList(tslist, map, 2) toList))
        }),

      MetricHandle(Unique, "certifiedUser") -> Metric(
        name = "Unique Users and New Certified Users",
        description = "The number of unique users and the number of new certified users during a period of time.",
        start = 7,
        end = 0,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val uData = metricReader.getUniqueCount("uniqueUser", interval, start, end)
          val cData = metricReader.getUniqueCount("certifiedUser", interval, start, end)
          val map = TimeDataPointUtil.createMergeMap(
                java.util.Arrays.asList(uData, cData), 2)
          val tslist = TimeDataPointUtil.getMergeTimeStampList(map)
          val uusers = TimeDataPointUtil.getMergeValueList(tslist, map, 0) toList
          val newcusers = TimeDataPointUtil.getMergeValueList(tslist, map, 1) toList

          DataSet(
            xLabel = Some("date"),
            yLabel = Some("unique users / new certified users"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(tslist toList),
            yHeaders = List("unique users", "new certified users"),
            yValues = List(uusers, newcusers))
        }),

      MetricHandle(Unique, "cumulativeCertifiedUser") -> Metric(
        name = "Cumulative Certified Users and New Certified Users",
        description = "The total number of certified users and the number of new certified users during a period of time.",
        start = 7,
        end = 0,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val cData = metricReader.getUniqueCount("certifiedUser", interval, start, end)
          val newcusers = cData map (d => d.y) toList
          val cusers = newcusers.map{ var s = "0"; d => { s = addString(s, d); s } }
          DataSet(
            xLabel = Some("date"),
            yLabel = Some("cumulative certified users / new certified users"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(cData map (d => d.x) toList),
            yHeaders = List("cumulative certified users", "new certified users"),
            yValues = List(cusers, newcusers))
        }),

      MetricHandle(Summary, "questions") -> Metric(
        name = "Question: Correct and Incorrect",
        description = "The number correct responses and incorrect responses for each question index.",
        start = 7,
        end = 0,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val x = (0 until 30) map (d => d toString) toList
          val pData = metricReader.getTotalCount("questionPassMetric", x asJava) asScala
          val fData = metricReader.getTotalCount("questionFailMetric", x asJava) asScala

          DataSet(
            xLabel = Some("questionIndex"),
            yLabel = Some("correct / incorrect"),
            xHeaders = List(DataHeader.ID),
            xValues = List(x),
            yHeaders = List("correct", "incorrect"),
            yValues = List(x map (k => pData(k)) toList,
                           x map (k => fData(k)) toList))
        })),
    
     "Tables" -> collection.immutable.ListMap(

      MetricHandle(Trending, "table-client-p") -> Metric(
        name = "Trending Table Clients (Percentage)",
        description = "List of trending Table Clients.",
        start = 30,
        end = 1,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val topData = metricReader.getTop("tableClient", interval, end, 0, 10)
          val trendingData = topData map (d => {
            metricReader.getCount("tableClient", d.id, interval, start, end)
          })
          val map = TimeDataPointUtil.createMergeMap(trendingData asJava, trendingData size)
          val tslist = TimeDataPointUtil.getMergeTimeStampList(map)
          DataSet(
            xLabel = Some("Date & Time"),
            yLabel = Some("Percentage"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(tslist toList),
            yHeaders = topData map (d => d.id) toList,
            yValues = (0 until (trendingData size)) map (d => 
              TimeDataPointUtil.getPercentageList(tslist, map, d) toList) toList)
        }), 
        
      MetricHandle(Trending, "table-uri-p") -> Metric(
        name = "Trending Table URI (Percentage)",
        description = "List of trending Table URI.",
        start = 30,
        end = 1,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val topData = metricReader.getTop("tableUri", interval, end, 0, 5)
          val trendingData = topData map (d => {
            metricReader.getCount("tableUri", d.id, interval, start, end)
          })
          val map = TimeDataPointUtil.createMergeMap(trendingData asJava, trendingData size)
          val tslist = TimeDataPointUtil.getMergeTimeStampList(map)
          DataSet(
            xLabel = Some("Date & Time"),
            yLabel = Some("Percentage"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(tslist toList),
            yHeaders = topData map (d => d.id) toList,
            yValues = (0 until (trendingData size)) map (d => 
              TimeDataPointUtil.getPercentageList(tslist, map, d) toList) toList)
        }),   
        
      MetricHandle(Trending, "table-client") -> Metric(
        name = "Trending Table Clients (Session Count)",
        description = "List of trending Table Clients.",
        start = 60,
        end = 1,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val topData = metricReader.getTop("tableClient", interval, end, 0, 10)
          val trendingData = topData map (d => {
            metricReader.getCount("tableClient", d.id, interval, start, end)
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
        
      MetricHandle(Trending, "table-uri") -> Metric(
        name = "Trending Table URI (Session Count)",
        description = "List of trending Table URI.",
        start = 60,
        end = 1,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val topData = metricReader.getTop("tableUri", interval, end, 0, 10)
          val trendingData = topData map (d => {
            metricReader.getCount("tableUri", d.id, interval, start, end)
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
         
      MetricHandle(Top, "topTables") -> Metric(
        name = "Top Tables (Session Count)",
        description = "List of the top 10 most accessed table entities.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val baseUrl = "https://www.synapse.org/#!Synapse:"
          val data = metricReader.getTop("uniqueTable", interval, start, page * 10, (page + 1) * 10)
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
        
      MetricHandle(Top, "topUpdateTables") -> Metric(
        name = "Top Updated Tables (Session Count)",
        description = "List of the top 10 most updated table entities.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val baseUrl = "https://www.synapse.org/#!Synapse:"
          val data = metricReader.getTop("updateTable", interval, start, page * 10, (page + 1) * 10)
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
        
      MetricHandle(Top, "topQueriedTables") -> Metric(
        name = "Top Queried Tables (Session Count)",
        description = "List of the top 10 most queried table entities.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val baseUrl = "https://www.synapse.org/#!Synapse:"
          val data = metricReader.getTop("queryTable", interval, start, page * 10, (page + 1) * 10)
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
        
      MetricHandle(Top, "topDownloadedTables") -> Metric(
        name = "Top Downloaded Tables (Session Count)",
        description = "List of the top 10 most downloaded table entities.",
        start = 1,
        end = 1,
        interval = Interval.month,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val baseUrl = "https://www.synapse.org/#!Synapse:"
          val data = metricReader.getTop("downloadTable", interval, start, page * 10, (page + 1) * 10)
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
         
      MetricHandle(Unique, "uniqueTable") -> Metric(
        name = "Count of Unique Tables",
        description = "The number of unique tables that have been touched per day/week/month.",
        start = 7,
        end = 0,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, text) => {
          val data = metricReader.getUniqueCount("uniqueTable", interval, start, end)
          DataSet(
            xLabel = Some("date"),
            yLabel = Some("count of unique users"),
            xHeaders = List(DataHeader.Timestamp),
            xValues = List(data map (d => d.x) toList),
            yHeaders = List("all tables"),
            yValues = List(data map (d => d.y) toList))
        })),    

    "Report" -> collection.immutable.ListMap(

      MetricHandle(Report, "fileDownloadReport") -> Metric(
        name = "File Download Report",
        description = "The list of users who downloaded a specific file.",
        start = 1,
        end = 1,
        interval = Interval.day,
        statistic = Statistic.n,
        dataSet = (start, end, interval, statistic, page, entityId) => {
          val baseUrl = "https://www.synapse.org/#!Profile:"
          val data = entityId match {
            case null | "" | "syn" => metricReader.getAllReport("fileDownloadReport", "somerandomstring")
            case _ => metricReader.getAllReport("fileDownloadReport", entityId)
          }

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

  def addString(a : String, b : String) = {
    var sum = (a toInt) + (b toInt)
    sum toString
  }
}
