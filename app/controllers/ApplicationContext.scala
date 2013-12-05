package controllers

import org.springframework.context.support.ClassPathXmlApplicationContext
import org.sagebionetworks.dashboard.model.MetricType
import org.sagebionetworks.dashboard.service.{MetricRegistry, MetricToRead}

object ApplicationContext {

  private val context = new ClassPathXmlApplicationContext("/META-INF/spring/scheduler-context.xml")
  private val registry = getBean(classOf[MetricRegistry])

  def getBean[T](clazz: Class[T]): T = {
    context.getBean(clazz)
  }

  def metrics: java.util.List[MetricToRead] = registry.metricsToRead

  def getMetric(id: String, metricType: MetricType): MetricToRead = registry.getMetric(id, metricType)
}
