package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class ByteRangeSpec extends WordSpec with MustMatchers {

  "Range: Should parse correctly on good input" in {
    HttpHeaders.Range(ByteRanges.parseByteRanges("bytes=0-500, 699-2000, -4").get).value must equal ("bytes=0-500, 699-2000, -4")
  }

  "Range: Should produce none on bad input" in {
    ByteRanges.parseByteRanges("bytes=cats") must equal (None)
    ByteRanges.parseByteRanges("bytes=1-29, cats").get.toString must equal ("bytes=1-29")
  }
}
