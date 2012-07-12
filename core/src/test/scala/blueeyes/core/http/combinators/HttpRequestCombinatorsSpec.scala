package blueeyes.core.http.combinators

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

import blueeyes.core.http.{HttpRequest, HttpResponse, HttpException, HttpStatus}
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.http.HttpMethods._
import blueeyes.core.http.test.HttpRequestMatchers
import blueeyes.json.JsonAST._
import akka.dispatch.Future
import blueeyes.concurrent.test.AkkaFutures

class HttpRequestCombinatorsSpec extends WordSpec with MustMatchers with HttpRequestCombinators with AkkaFutures with blueeyes.bkka.AkkaDefaults {
  type Handler[T, S] = HttpRequest[Future[T]] => Future[HttpResponse[S]]
  
  "refineContentType should return bad request type cannot be refined to specified subtype" in {
    jObjectCaller { refineContentType { jIntHandler } }.futureValue match {
      case HttpResponse(status, _, _, _) => status.code must equal (BadRequest)
      case other => fail("Expected BadRequest, but got " + other)
    }
  }
  
  "refineContentType should refine content type when possible" in {
    jIntCaller { refineContentType { jIntHandler } }.futureValue match {
      case HttpResponse(status, _, Some(content), _) => 
        status.code must equal (OK)
        content must equal (JInt(123))
      case other => fail("Expected OK Request with content, but got " + other)
    }
  }
  
  def jObjectCaller(h: Handler[JValue, JValue]) = h(HttpRequest(uri = "/", method = GET, content = Some(Future(JObject(Nil)))))
  
  def jIntCaller(h: Handler[JValue, JValue]) = h(HttpRequest(uri = "/", method = GET, content = Some(Future(JInt(123)))))
  
  def jIntHandler(r: HttpRequest[Future[JInt]]): Future[HttpResponse[JValue]] = Future(HttpResponse(content = Some(JInt(123): JValue)))
}
