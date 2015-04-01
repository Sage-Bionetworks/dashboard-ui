package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.sagebionetworks.dashboard.config.DashboardConfig

import play.api.Logger
import play.api.Play.current
import play.api.cache.Cache
import play.api.mvc.ActionBuilder
import play.api.mvc.Request
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

import context.SpringContext

trait Security {

  object AuthorizedAction extends ActionBuilder[Request] {

    private val tokenKey = "dbdtoken"
    private val tokenExpire = 3 // Expire after 3 hours on the server side

    def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]) = {
      request.session.get(tokenKey).map { token =>
        Cache.get(token).map { t =>
          Logger.info("Session tokens match, continue with the request.")
          block(request).map { result =>
            // Save the session token in the response
            result.withSession(tokenKey -> token)
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
      val googleClientId = SpringContext.getBean(classOf[DashboardConfig]).getGoogleClientId
      val schemeHost = scheme(request) + "://" + request.host
      val state = schemeHost + "/" + request.uri
      val redirectUri = schemeHost + "/google/oauth2callback"
      val url = "https://accounts.google.com/o/oauth2/auth?response_type=code&" +
        "client_id=" + urlEncode(googleClientId) + "&" +
        "redirect_uri=" + urlEncode(redirectUri) + "&" +
        "scope=" + urlEncode("https://www.googleapis.com/auth/userinfo.email") + "&" +
        "state=" + urlEncode(state)
      Future.successful(Redirect(url))
    }

    private def urlEncode(url: String) = {
      java.net.URLEncoder.encode(url, "UTF-8")
    }

    private def scheme[A](request: Request[A]) = {
      // $X-Scheme is set by the nginx reverse proxy
      request.headers.get("X-Scheme") match {
        case Some(scheme) => scheme
        case None => "http"
      }
    }
  }
}
