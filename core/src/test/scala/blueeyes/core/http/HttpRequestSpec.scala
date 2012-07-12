package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class HttpRequestSpec extends WordSpec with MustMatchers {
  "HttpRequest.apply" should {
    "parse query string properly" in {
      val ps = HttpRequest(method = HttpMethods.GET, uri = "http://foo.com?a=b&c=d&e").parameters

      ps must equal (Map(
        'a -> "b",
        'c -> "d",
        'e -> ""
      ))
    }

    "override parameters with query string parameters" in {
      val ps = HttpRequest(method = HttpMethods.GET, uri = "http://foo.com?a=b", parameters = Map('a -> "z")).parameters

      ps must equal (Map(
        'a -> "b"
      ))
    }

    "not doubly unescape query parameters" in {
      val ps = HttpRequest(method = HttpMethods.GET, uri = "http://foo.com?a=a%26b%26c%26d").parameters

      ps must equal (Map(
        'a -> "a&b&c&d"
      ))
    }

    "allow no query string" in {
      val ps = HttpRequest(method = HttpMethods.GET, uri = "http://foo.com").parameters

      ps must equal (Map.empty[Symbol, String])
    }

    "allow empty query string" in {
      val ps = HttpRequest(method = HttpMethods.GET, uri = "http://foo.com?").parameters

      ps must equal (Map.empty[Symbol, String])
    }
  }
}
