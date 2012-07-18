package blueeyes.core.service.test

//import org.specs2.mutable.Specification
import blueeyes.core.service._
import akka.dispatch.Future
import akka.dispatch.Promise
import blueeyes.core.http.HttpResponse
import blueeyes.core.http.MimeTypes._
import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.http.MimeTypes._
import blueeyes.core.http._
import blueeyes.core.data.{ByteChunk, BijectionsChunkString}
import TestService._
//import org.specs2.time.TimeConversions._
import blueeyes.concurrent.test.AkkaFutures
import org.scalatest.time._
import org.scalatest.concurrent.Eventually


class BlueEyesServiceSpecificationSpec extends BlueEyesServiceSpec with Eventually with TestService with BijectionsChunkString with AkkaFutures{
  override implicit val defaultPatience =   PatienceConfig(timeout =  Span(6, Seconds), interval = Span(1, Second))

  "Service Specification" should {
    "support get by valid URL" in {
      service.get[String]("/bar/id/bar.html").futureValue must equal (serviceResponse)
    }

    "support asynch get by valid URL" in {
      val result = service.get[String]("/asynch/future") 
      result.futureValue must equal (serviceResponse)
    }

    "support eventually asynch get by valid URL" in {
      eventually { 
        service.get[String]("/asynch/eventually").futureValue must equal (serviceResponse)
      }
    }
  }
}

trait TestService extends BlueEyesServiceBuilder with BijectionsChunkString {
  private var eventuallyCondition = false
  val sampleService = service("sample", "1.32") { context =>
    request {
      produce(text/html) {
        path("/bar/'foo/bar.html") {
          get { request: HttpRequest[ByteChunk] =>
            Future(serviceResponse)
          }
        }~
        path("/asynch/future") {
          get { request: HttpRequest[ByteChunk] =>
            async {
              serviceResponse
            }
          }
        }~
        path("/asynch/eventually") {
          get { request: HttpRequest[ByteChunk] =>
            if (eventuallyCondition) {
              Future(serviceResponse)
            } else {
              eventuallyCondition = true
              Promise.successful(HttpResponse[String](HttpStatus(HttpStatusCodes.NotFound)))
            }
          }
        }
      }
    }
  }

  private def async[T](f: => T): Future[T] = {
    val promise = akka.dispatch.Promise[T]()

    import scala.actors.Actor.actor
    actor {
      Thread.sleep(5000)
      promise.success(f)
    }

    promise
  }
} 

object TestService{
  val serviceResponse = HttpResponse[String](HttpStatus(HttpStatusCodes.OK), Map("Content-Type" -> "text/html"), Some("context"), HttpVersions.`HTTP/1.1`)
}
