package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import TCodings._

class TCodingSpec extends WordSpec with MustMatchers {

  "TCoding:  Should parse \"trailers, deflate\" as (trailers. deflate) produce Nil for \"12\"" in {
    TCodings.parseTCodings("trailers, deflate") must equal (List(trailers, deflate))
    TCodings.parseTCodings("12") must equal (Nil)
  }

  "TCoding:  Should parse CustomTCoding" in {
    TCodings.parseTCodings("foo, bar") must equal (List(CustomTCoding("foo"), CustomTCoding("bar")))
  }
}
