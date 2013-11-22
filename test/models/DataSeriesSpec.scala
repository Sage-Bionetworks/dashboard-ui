package models

import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json._

@RunWith(classOf[JUnitRunner])
class DataSeriesSpec extends Specification {

  "DataSeries.toJson" should {

    val name = "User Count"
    val headers = List("Day", "All", "Non-Sage")
    val values = List(
      List("10-21", "366", "305"),
      List("10-22", "372", "311")
    )
    val dataSeries = new DataSeries(name, "description", headers, values)
    val json = dataSeries.toJson
    val jsonString = Json.stringify(json)

    println(jsonString)

    "have the correct name" in {
      val jsName = json\"name"
      jsName.as[String] must beEqualTo("User Count")
    }

    "output the correct string" in {
      jsonString must beEqualTo("""{"name":"User Count","headers":["Day","All","Non-Sage"],"values":[["10-21","366","305"],["10-22","372","311"]]}""")
    }
  }
}
