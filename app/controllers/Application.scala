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
}
