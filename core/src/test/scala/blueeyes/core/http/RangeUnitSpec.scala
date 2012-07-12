package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class RangeUnitSpec extends WordSpec with MustMatchers {

  "Range-Units:  Should parse \"bytes\" as Some(bytes) produce None for \"cats\"" in {
    RangeUnits.parseRangeUnits("bytes").map(_.toString).getOrElse("") must equal ("bytes")
    RangeUnits.parseRangeUnits("cats").map(_.toString).getOrElse("") must equal ("")
  }

  "Accept-Ranges:  Should create none from \"none\"" in {
    HttpHeaders.`Accept-Ranges`(RangeUnits.parseRangeUnits("none").get).value must equal ("none")
  }
}
