import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get
      status(home) must equalTo(SEE_OTHER)
    }

    "render the metric list page" in new WithApplication{
      val metrics = route(FakeRequest(GET, "/metrics")).get
      status(metrics) must equalTo(OK)
      contentAsString(metrics) must contain ("Top")
      contentAsString(metrics) must contain ("Latencies")
    }
  }
}
