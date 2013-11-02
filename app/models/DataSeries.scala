package models

case class DataSeries (
  name: String, headers: List[String], values: List[List[String]]
) {
   def toJson: String = {
    ""
  }
}

object DataSeries {
  // TODO: Call the dashboard service here
}
