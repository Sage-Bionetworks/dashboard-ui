package context

import org.springframework.context.support.ClassPathXmlApplicationContext

object AppContext {

  private val context = new ClassPathXmlApplicationContext(
      "/META-INF/spring/scheduler-context.xml")

  def start = {
    context.start
  }

  def close = {
    context.close
  }

  def getBean[T](clazz: Class[T]): T = {
    context.getBean(clazz)
  }
}
