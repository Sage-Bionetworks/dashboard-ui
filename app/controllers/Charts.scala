package controllers

import play.api.mvc.{Action, Controller}
import models.DataSeries

object Charts extends Controller {

  def bar(metric: String, start: Option[String], end: Option[String], interval: Option[String], stat: Option[String]) = Action {
    val dataSeries = new DataSeries(
      name = "metric name",
      headers = List[String](),
      values = List[List[String]]()
    )
    Ok(views.html.barchart(dataSeries))
  }

  def hbar(metric: String, start: Option[String], end: Option[String], interval: Option[String], stat: Option[String]) = Action {
    val dataSeries = new DataSeries(
      name = "metric name",
      headers = List[String](),
      values = List[List[String]]()
    )
    Ok(views.html.barchart(dataSeries))
  }

  def line(metric: String, start: Option[String], end: Option[String], interval: Option[String], stat: Option[String]) = Action {
    val dataSeries = new DataSeries(
      name = "metric name",
      headers = List[String](),
      values = List[List[String]]()
    )
    Ok(views.html.barchart(dataSeries))
  }
}
