package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class HttpUriSpec extends WordSpec with MustMatchers {
  "Host:  Should parse correct host uri" in {
    HttpHeaders.Host(URI.opt("http://www.socialmedia.com/coolServer/index.html").get).value must equal ("www.socialmedia.com")
  }

  "Host:  Should parse correct host uri also" in {
    HttpHeaders.Host(URI.opt("http://maps.google.com/coolmap.html").get).value must equal ("maps.google.com")
  }

  "Host:  Should parse correct host uri with port" in {
    URI.opt("http://maps.google.com:8080/coolmap.html").get must equal (URI("http://maps.google.com:8080/coolmap.html"))
  }

  "Location: Should return correct url on parsed input" in {
    HttpHeaders.`Location`(URI.opt("  http://www.socialmedia.com/index.html  ").get).value must equal ("http://www.socialmedia.com/index.html")
  }
}
