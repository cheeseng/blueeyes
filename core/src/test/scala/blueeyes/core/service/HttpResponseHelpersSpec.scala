package blueeyes.core.service

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import blueeyes.core.http._
import blueeyes.core.http.test._
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.util.RichThrowableImplicits._
import akka.dispatch.Future
import akka.dispatch.Promise

class HttpResponseHelpersSpec extends WordSpec with MustMatchers with HttpResponseHelpers with HttpRequestCheckers {
  
  "HttpResponseHelpers respond: creates Future with the specified parameters" in {
    val statusCode  = InternalServerError
    val headers = Map("foo" -> "bar")
    val content = "zoo"

    respond(HttpStatus(statusCode), headers, Some(content)).futureValue match {
      case HttpResponse(HttpStatus(code, _), HttpHeaders(h), c, _) => 
          code must equal (statusCode) 
          h must equal (headers) 
          c must be (Some(content))
      case other => fail("Expected HttpResponse, but got: " + other)
    }
  }

  "HttpResponseHelpers respondLater: creates Future when response is OK" in {
    val headers = Map("foo" -> "bar")
    val content = "zoo"

    respondLater[String](Future(content), headers).futureValue match {
      case HttpResponse(HttpStatus(code, _), HttpHeaders(h), c, _) => 
        code must equal (OK) 
        h must equal (headers) 
        c must be (Some(content))
      case other => fail("Expected HttpResponse, but got: " + other)
    }
  }

  "HttpResponseHelpers respondLater: creates Future when response is error (Future is cancelled with error)" in {
    val error   = new NullPointerException()
    val promise = Promise.failed[String](error)
    respondWithCode(respondLater[String](promise), InternalServerError)
  }

  "HttpResponseHelpers respondLater: creates Future when response is error (Future is cancelled without error)" in {
    val error   = new NullPointerException()
    val promise = Promise[String]
    promise.failure(new RuntimeException())
    respondWithCode(respondLater[String](promise), InternalServerError)
  }
}
