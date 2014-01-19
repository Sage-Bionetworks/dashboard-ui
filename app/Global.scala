import play.api.{GlobalSettings, Logger}
import context.SpringContext

object Global extends GlobalSettings {

  override def onStart(app: play.api.Application) {
    SpringContext.start
  }

  override def onStop(app: play.api.Application) {
    SpringContext.close
  }
}
