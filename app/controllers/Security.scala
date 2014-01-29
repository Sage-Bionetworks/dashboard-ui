package controllers

import org.openid4java.consumer.ConsumerManager
import org.openid4java.message.AuthSuccess
import org.openid4java.message.ParameterList
import org.openid4java.message.ax.AxMessage
import org.openid4java.message.ax.FetchRequest
import org.openid4java.message.ax.FetchResponse
import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration.HOURS
import play.api.Logger
import play.api.Play.current
import play.api.cache.Cache
import play.api.mvc.ActionBuilder
import play.api.mvc.Request
import play.api.mvc.Results.Redirect
import play.api.mvc.Results.Unauthorized
import play.api.mvc.SimpleResult

trait Security {

  object AuthorizedAction extends ActionBuilder[Request] {

    private object GoogleOpenId {
      val ProviderUrl = "https://www.google.com/accounts/o8/id"
      val Manager = new ConsumerManager
      val Discovered = Manager.associate(Manager.discover(ProviderUrl))
      val CallbackQueryParameter = "openid.mode"
    }

    private val tokenKey = "token"
    private val tokenExpire = 3 // Expire after 3 hours on the server side

    def invokeBlock[A](request: Request[A], block: Request[A] => Future[SimpleResult]) = {
      request.session.get(tokenKey).map { token =>
        Cache.get(token).map { t =>
          Logger.info("Session tokens match, continue with the request.")
          block(request).map { result =>
            // Save the session token in the response
            result.withSession(tokenKey -> token)
          }
        } getOrElse {
          Logger.info("Missing session on the server side, log in.")
          login(request, block)
        }
      } getOrElse {
        Logger.info("Missing session in the request, log in.")
        login(request, block)
      }
    }

    private def login[A](request: Request[A], block: Request[A] => Future[SimpleResult]) = {
      // Check if this is OpenID callback
      request.queryString.get(GoogleOpenId.CallbackQueryParameter) match {
        case Some(p) => openIdCallback(request, block)
        case None => openIdLogin(request) // When this is not a callback, log in 
      }
    }

    /**
     * Verifies the OpenID login and reads the user email address.
     */
    private def openIdCallback[A](request: Request[A], block: Request[A] => Future[SimpleResult]) = {
      val receivingURL = scheme(request) + "://" + request.host + request.uri
      val parameterMap = new ParameterList(request.queryString.map { case (k,v) => k -> v.mkString })
      val verification = GoogleOpenId.Manager.verify(
          receivingURL.toString(),
          parameterMap,
          GoogleOpenId.Discovered)
      val verifiedId = verification.getVerifiedId()
      if (verifiedId == null) {
        // Invalid callback, log in again
        openIdLogin(request)
      } else {
        val authSuccess = verification.getAuthResponse().asInstanceOf[AuthSuccess]
        val fetchResp = authSuccess.getExtension(AxMessage.OPENID_NS_AX).asInstanceOf[FetchResponse]
        val emails = fetchResp.getAttributeValues("email")
        val email = emails.get(0).toString
        if (email == null) {
          Future.successful(Unauthorized("Missing email address from the login."))
        } else if (isAuthorized(email)) {
          // Save the session on the server side
          val session = java.util.UUID.randomUUID.toString
          Cache.set(session, session, Duration(tokenExpire, HOURS))
          // Continue with the original request
          // and save the session token in the response
          block(request).map { result =>
            result.withSession(tokenKey -> session)
          }
        } else {
          Future.successful(Unauthorized(email + " is not authorized to access dashboard."))
        }
      }
    }

    /**
     * Logs in the user using Google OpenID 2.0.
     */
    private def openIdLogin[A](request: Request[A]) = {
      // Skip the query parameters if they are polluted with 'openid' parameters
      val skipQueryStr = request.queryString.filterKeys(key => key.startsWith("openid")).size > 0
      val uri = skipQueryStr match {
        case true => "/"
        case false => request.uri
      }
      val returnToUrl = scheme(request) + "://" + request.host + uri
      val authReq = GoogleOpenId.Manager.authenticate(GoogleOpenId.Discovered, returnToUrl)
      val fetchReq = FetchRequest.createFetchRequest
      fetchReq.addAttribute(
          "email",
          "http://schema.openid.net/contact/email",
          true)
      authReq.addExtension(fetchReq)
      Future.successful(Redirect(authReq.getDestinationUrl(true)))
    }

    /**
     * Authorizes based on Synapse user info.
     */
    private def isAuthorized(email: String) = {
      email != null && email.toLowerCase().endsWith("sagebase.org");
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
