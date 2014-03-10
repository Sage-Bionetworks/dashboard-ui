package models

import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json._

@RunWith(classOf[JUnitRunner])
class DataSetSpec extends Specification {

  "DataSet.toJson" should {

    val json = DataSet(
      xLabel = Some("user activities"),
      yLabel = None,
      xHeaders = List(DataHeader.ID, DataHeader.Name),
      xValues = List(List("23", "51", "20"), List("Alice", "Bob", "Carol")),
      yHeaders = List("days", "sessions"),
      yValues = List(List("2", "15", "7"), List("25", "15670", "924"))
    ).json

    "should have the correct xLabel" in {
      val xLabel = json \ "xLabel"
      xLabel.as[String] must beEqualTo("user activities")
    }

    "should be missing the yLabel" in {
      val yLabel = json \ "yLabel"
      yLabel must beEqualTo(JsNull)
    }

    "should have the correct xHeaders" in {
      val xHeaders = json \ "xHeaders"
      Json.stringify(xHeaders) must beEqualTo("""["id","name"]""")
    }

    "should have the correct xValues" in {
      val xValues = json \ "xValues"
      Json.stringify(xValues) must beEqualTo("""[["23","51","20"],["Alice","Bob","Carol"]]""")
    }

    "should have the correct yHeaders" in {
      val yHeaders = json \ "yHeaders"
      Json.stringify(yHeaders) must beEqualTo("""["days","sessions"]""")
    }

    "should have the correct yValues" in {
      val yValues = json \ "yValues"
      Json.stringify(yValues) must beEqualTo("""[["2","15","7"],["25","15670","924"]]""")
    }
  }
}
