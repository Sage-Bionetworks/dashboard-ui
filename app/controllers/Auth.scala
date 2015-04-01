package controllers

import org.sagebionetworks.dashboard.config.DashboardConfig

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WS
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Request

import context.SpringContext

object Auth extends Controller {

  private object Whitelist {
    val whitelist = SpringContext.getBean(
        classOf[DashboardConfig]).getUserWhitelist.split(":").toSet
    def contains(item: String) = {
      whitelist.contains(item)
    }
  }

  /**
   * Authenticates and authorizes.
   */
  def googleOAuth2 = Action.async { implicit request =>
    val state = request.queryString.get("state") match {
      case Some(state) => state(0)
      case None => "/"
    }
    request.queryString.get("code") match {
      case None => scala.concurrent.Future(Unauthorized("Google OAuth 2 failed."))
      case Some(code) => {
        val endPoint = "https://www.googleapis.com/oauth2/v3/token"
        val clientId = SpringContext.getBean(classOf[DashboardConfig]).getGoogleClientId
        val clientSecret = SpringContext.getBean(classOf[DashboardConfig]).getGoogleClientSecret
        val redirectUri = scheme(request) + "://" + request.host + "/google/oauth2callback"
        val body = Map("code" -> Seq((code(0))),
          "client_id" -> Seq((clientId)),
          "client_secret" -> Seq((clientSecret)),
          "redirect_uri" -> Seq((redirectUri)),
          "grant_type" -> Seq(("authorization_code")))
        WS.url(endPoint).post(body).map { response =>
          Ok(response.json \ "access_token")
        }
      } 
    }
  }

  private def scheme[A](request: Request[A]) = {
    // $X-Scheme is set by the nginx reverse proxy
    request.headers.get("X-Scheme") match {
      case Some(scheme) => scheme
      case None => "http"
    }
  }

  private def isAuthorizedByEmail(email: String) = {
    email != null && (email.toLowerCase().endsWith("sagebase.org") || Whitelist.contains(email));
  }

  private def isAuthorizedById(id: String) = {
    id != null && Whitelist.contains(id);
  }
}
