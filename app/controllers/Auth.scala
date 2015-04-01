package controllers

import org.sagebionetworks.dashboard.config.DashboardConfig

import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._

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
      case None => Unauthorized("Google OAuth failed at obtaining authorization code.")
      case Some(code) => {
        getIdToken(request, code(0))
      }
    }
  }

  private def getIdToken[A](request: Request[A], code: String) = {
    val endPoint = "https://www.googleapis.com/oauth2/v3/token"
    val clientId = config.getGoogleClientId
    val clientSecret = config.getGoogleClientSecret
    val redirectUri = scheme(request) + "://" + request.host + "/google/oauth2callback"
    val body = Map(
      "code" -> Seq(code),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(clientSecret),
      "redirect_uri" -> Seq(redirectUri),
      "grant_type" -> Seq("authorization_code"))
    val idToken = WS.url(endPoint).post(body) map { response =>
      (response.json \ "id_token").as[String]
    }
    idToken onSuccess {
      case idToken => decryptIdToken(idToken)
    }
    Unauthorized("Google OAuth failed at obtaining id token.")
  }

  private def decryptIdToken(idToken: String) = {
    val url = "https://www.googleapis.com/oauth2/v1/tokeninfo"
    val userId = WS.url(url).withQueryString("id_token" -> idToken).get map {
      response => response.json
    }
    userId onSuccess {
      case userInfo => checkUserEmail(userInfo)
    }
    Unauthorized("Google OAuth failed decrypting id token.")
  }

  def checkUserEmail(userInfo: JsValue) {
    Logger.info(userInfo.toString)
  }

  private def isAuthorizedByEmail(email: String) = {
    email != null && (email.toLowerCase().endsWith("sagebase.org") || Whitelist.contains(email));
  }

  private def isAuthorizedById(id: String) = {
    id != null && Whitelist.contains(id);
  }
}
