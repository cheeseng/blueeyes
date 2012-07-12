package blueeyes.core.http
package test

import blueeyes.concurrent.test.AkkaFutures
import org.scalatest.Suite
import org.scalatest.matchers.MustMatchers
import akka.dispatch.Future
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.http.HttpStatusCode
import blueeyes.core.http.HttpResponse

trait HttpRequestCheckers extends AkkaFutures { thisSuite: Suite with MustMatchers =>
  def succeedWithContent[A](future: Future[HttpResponse[A]], expected: Any) {
    future.futureValue match {
      case HttpResponse(status, _, Some(content), _) => 
        status.code must equal (OK)
        content must equal (expected)
      case other => fail("Expected OK Request with content, but got " + other)
    }
  }

  def respondWithCode[A](future: Future[HttpResponse[A]], expected: HttpStatusCode) {
    future.futureValue match {
      case HttpResponse(status, _, _, _) => status.code must equal (expected)
      case other => fail("Expected BadRequest, but got " + other)
    }
  }
}
