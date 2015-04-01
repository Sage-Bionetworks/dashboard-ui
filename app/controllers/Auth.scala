package controllers

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Success, Failure}

import play.api.cache.Cache
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._
import play.api.mvc.Results._

import org.sagebionetworks.dashboard.config.DashboardConfig
import context.SpringContext

object Auth extends Controller with Security {

  private object Whitelist {
    val whitelist = config.getUserWhitelist.split(":").toSet
    def contains(item: String) = {
      whitelist.contains(item)
    }
  }

  /**
   * Authenticates and authorizes using Google OAuth 2.
   */
  def googleOAuth2 = Action { implicit request =>
    request.queryString.get("code") match {
      case Some(code) => getIdToken(request, code(0))
      case None => Unauthorized("Google OAuth failed at obtaining authorization code.")
    }
  }

  private def getIdToken[A](request: Request[A], code: String) = {
    val endPoint = "https://www.googleapis.com/oauth2/v3/token"
    val redirectUri = scheme(request) + "://" + request.host + "/google/oauth2callback"
    val body = Map(
      "code" -> Seq(code),
      "client_id" -> Seq(config.getGoogleClientId),
      "client_secret" -> Seq(config.getGoogleClientSecret),
      "redirect_uri" -> Seq(redirectUri),
      "grant_type" -> Seq("authorization_code"))
    val idToken = WS.url(endPoint).post(body) map { response =>
      (response.json \ "id_token").asOpt[String]
    }
    Await.ready(idToken, 2 seconds).value.get match {
      case Success(idToken) => {
        idToken match {
          case Some(idToken) => decryptIdToken(request, idToken)
          case None => Unauthorized("Google OAuth failed at obtaining id token.")
        }
      }
      case Failure(t) => {
        Logger.info(t.getMessage)
        Unauthorized("Google OAuth failed at obtaining id token.")
      }
    }
  }

  private def decryptIdToken[A](request: Request[A], idToken: String) = {
    val url = "https://www.googleapis.com/oauth2/v1/tokeninfo"
    val userInfo = WS.url(url).withQueryString("id_token" -> idToken).get map {
      response => response.json
    }
    Await.ready(userInfo, 10 seconds).value.get match {
      case Success(userInfo) => {
        authorizeByUserEmail(request, userInfo)
      }
      case Failure(t) => {
        Logger.info(t.getMessage)
        Unauthorized("Google OAuth failed at decrypting id token.")
      }
    }
  }

  def authorizeByUserEmail[A](request: Request[A], userInfo: JsValue) = {
    (userInfo \ "email").asOpt[String] match {
      case Some(email) => {
        if (isAuthorizedByEmail(email) || isInWhitelist(email)) {
          // Create a session token and save it on the server side
          val sessionToken = java.util.UUID.randomUUID.toString
          Cache.set(sessionToken, sessionToken, Duration(sessionTokenExpireHours, HOURS))
          // State is what the user intended to do before being diverted to logging in
          val state = request.queryString.get("state") match {
            case Some(state) => state(0)
            case None => scheme(request) + "://" + request.host + "/"
          }
          // Redirect user back to the original request with a new session
          Redirect(state).withSession(sessionTokenKey -> sessionToken)
        } else {
          Unauthorized(email + " is not allowed.")
        }
      }
      case None => Unauthorized("Google OAuth failed at obtaining user email.")
    }
  }

  private def isAuthorizedByEmail(email: String) = {
    email != null && (email.toLowerCase().endsWith("sagebase.org") || Whitelist.contains(email));
  }

  private def isInWhitelist(id: String) = {
    id != null && Whitelist.contains(id);
  }
}
