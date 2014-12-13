import play.api.{Application, GlobalSettings, Logger}
import play.api.http.HeaderNames.X_FORWARDED_FOR
import play.api.mvc.{Action, Handler, RequestHeader}
import play.api.mvc.Results.Redirect
import context.SpringContext

// Notes:
// 1) This must be in the default package.
// 2) There is a corresponding change in application.conf.
// 3) The DEV mode (the run command) does not support this.
object Global extends GlobalSettings {
  override def beforeStart(app: Application) {
    SpringContext.start
  }
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    request.headers.get(X_FORWARDED_FOR) match {
      case ip:Any => Logger.info("X-Forwarded-For: " + ip)
      super.onRouteRequest(request)
    }
  }
}
