package context

import play.api.GlobalSettings

object Global extends GlobalSettings {

  import org.springframework.context.support.ClassPathXmlApplicationContext

  override def onStart(app: play.api.Application) {
    AppContext.start
  }

  override def onStop(app: play.api.Application) {
    AppContext.close
  }
}
