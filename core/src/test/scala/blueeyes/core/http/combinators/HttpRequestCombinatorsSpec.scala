package blueeyes.core.http.combinators

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

import blueeyes.core.http.{HttpRequest, HttpResponse, HttpException, HttpStatus}
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.http.HttpMethods._
import blueeyes.json.JsonAST._
import akka.dispatch.Future
import blueeyes.core.http.test.HttpRequestCheckers

class HttpRequestCombinatorsSpec extends WordSpec with MustMatchers with HttpRequestCombinators with HttpRequestCheckers with blueeyes.bkka.AkkaDefaults {
  type Handler[T, S] = HttpRequest[Future[T]] => Future[HttpResponse[S]]
  
  "refineContentType should return bad request type cannot be refined to specified subtype" in {
    respondWithCode(jObjectCaller { refineContentType { jIntHandler } }, BadRequest)
  }
  
  "refineContentType should refine content type when possible" in {
    succeedWithContent(jIntCaller { refineContentType { jIntHandler } }, JInt(123))
  }
  
  def jObjectCaller(h: Handler[JValue, JValue]) = h(HttpRequest(uri = "/", method = GET, content = Some(Future(JObject(Nil)))))
  
  def jIntCaller(h: Handler[JValue, JValue]) = h(HttpRequest(uri = "/", method = GET, content = Some(Future(JInt(123)))))
  
  def jIntHandler(r: HttpRequest[Future[JInt]]): Future[HttpResponse[JValue]] = Future(HttpResponse(content = Some(JInt(123): JValue)))
}
