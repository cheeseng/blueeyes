package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import HttpHeaders._

class HttpHeadersSpec extends WordSpec with MustMatchers {
  "HttpHeaders" should {
    "find headers by type" in{
      val headers = HttpHeaders(List("authorization" -> "foo"))
      headers.header[Authorization] must be (Some(Authorization("foo")))
    }

    "extract known header types" in {
      val headers = Map("authorization" -> "foo")

      val auths = for (Authorization(auth) <- headers) yield auth
      auths must equal (List("foo")) 
    }
    "extract Host header types without scheme" in {
      val headers = Map("Host" -> "localhost:8585")

      val hosts = for (Host(host) <- headers) yield host
      hosts.map(Host(_).value) must equal (List("localhost:8585"))
    }

    "parse custom tuples" in {
      HttpHeader(("Blargh" -> "foo")) must equal (CustomHeader("Blargh", "foo"))
    }
  }
}
