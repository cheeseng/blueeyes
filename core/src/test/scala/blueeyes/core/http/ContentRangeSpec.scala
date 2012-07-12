package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class ContentRangeSpec extends WordSpec with MustMatchers {

  "Content-Range: Should parse bytes=1234-5678/1212 correctly" in {
    HttpHeaders.`Content-Range`(ContentByteRanges.parseContentByteRanges("bytes=1234-5678/1212").get).value must equal ("bytes=1234-5678/1212")
  }

  "Content-Range: Should parse bytes=1234-5678 to None"  in {
    ContentByteRanges.parseContentByteRanges("bleh=1234-5678") must equal (None)
  }

}

