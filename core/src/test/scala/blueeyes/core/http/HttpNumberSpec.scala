package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class HttpNumberSpec extends WordSpec with MustMatchers {

  "HttpNumbers:  Should return ContentLength or parse to None on bad input" in {
    HttpNumbers.parseHttpNumbers("bees") must equal (None)
  }
}
