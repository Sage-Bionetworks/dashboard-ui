import play.api.{GlobalSettings, Logger}
import context.SpringContext

// Notes:
// 1) This must be in the default package.
// 2) There is a corresponding change in application.conf.
// 3) The DEV mode (the run command) does not support this.
object Global extends GlobalSettings {

  override def onStart(app: play.api.Application) {
    SpringContext.start
  }
}
