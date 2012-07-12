package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class IfRangeSpec extends WordSpec with MustMatchers {

  "If-Range:  should return an HttpDateTime from an HttpDateTime input" in {
    HttpHeaders.`If-Range`(IfRanges.parseIfRanges("Tue, 29 Dec 2009 12:12:12 GMT").get).value must equal ("Tue, 29 Dec 2009 12:12:12 GMT")
  }

  "If-Range:  should return an HttpDateTime from an HttpDateTime input also " in {
    HttpHeaders.`If-Range`(IfRanges.parseIfRanges("\"e-tag content\"").get).value must equal ("\"e-tag content\"")
  }

}

