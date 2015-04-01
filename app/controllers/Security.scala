package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.sagebionetworks.dashboard.config.DashboardConfig

import play.api.Logger
import play.api.Play.current
import play.api.cache.Cache
import play.api.mvc._
import play.api.mvc.Results._

import context.SpringContext

trait Security {

  val sessionTokenKey = "dbd-token"
  val sessionTokenExpireHours = 24
  val config = SpringContext.getBean(classOf[DashboardConfig])

  def scheme[A](request: Request[A]) = {
    // $X-Scheme is set by the nginx reverse proxy
    request.headers.get("X-Scheme") match {
      case Some(scheme) => scheme
      case None => "http"
    }
  }

  def urlEncode(url: String) = {
    java.net.URLEncoder.encode(url, "UTF-8")
  }

  object AuthorizedAction extends ActionBuilder[Request] {

    def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]) = {
      request.session.get(sessionTokenKey).map { token =>
        Cache.get(token).map { t =>
          Logger.info("Session tokens match, continue with the request.")
          block(request).map { result =>
            // Save the session token in the response
            result.withSession(sessionTokenKey -> token)
          }
        } getOrElse {
          Logger.info("Missing session on the server side, log in.")
          googleOAuth2(request, block)
        }
      } getOrElse {
        Logger.info("Missing session in the request, log in.")
        googleOAuth2(request, block)
      }
    }

    private def googleOAuth2[A](request: Request[A], block: Request[A] => Future[Result]) = {
      val googleClientId = config.getGoogleClientId
      val schemeHost = scheme(request) + "://" + request.host
      val state = schemeHost + "/" + request.uri
      val redirectUri = schemeHost + "/google/oauth2callback"
      val url = "https://accounts.google.com/o/oauth2/auth?response_type=code&" +
        "client_id=" + urlEncode(googleClientId) + "&" +
        "redirect_uri=" + urlEncode(redirectUri) + "&" +
        "scope=" + urlEncode("https://www.googleapis.com/auth/userinfo.email") + "&" +
        "state=" + urlEncode(state)
      Future(Redirect(url))
    }
  }
}
