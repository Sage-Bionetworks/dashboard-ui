package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok("dashboard")
  }

  def latency = Action {
    Ok(views.html.latency())
  }

  def series = Action {
    Ok(views.html.series())
  }

  def drilldown = Action {
    Ok(views.html.drilldown())
  }

  def counts = Action {
    Ok(views.html.counts())
  }
}
