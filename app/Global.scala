import play.api._
import org.springframework.context.support._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    val ctx = new ClassPathXmlApplicationContext("/META-INF/spring/app-context.xml")
  }
}
