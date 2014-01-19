
package context

import org.springframework.context.support.ClassPathXmlApplicationContext
import play.api.Logger

object SpringContext {

  private val context = new ClassPathXmlApplicationContext(
      "/META-INF/spring/scheduler-context.xml")

  def start = {
    Logger.info("Starting Spring context...")
    context.start
  }

  def close = {
    Logger.info("Closing Spring context...")
    context.close
  }

  def getBean[T](clazz: Class[T]): T = {
    context.getBean(clazz)
  }
}
