package controllers

import org.springframework.context.support.ClassPathXmlApplicationContext

object ApplicationContext {

  private val context = new ClassPathXmlApplicationContext("/META-INF/spring/app-context.xml")

  def getBean[T](clazz: Class[T]): T = {
    context.getBean(clazz)
  }
}
