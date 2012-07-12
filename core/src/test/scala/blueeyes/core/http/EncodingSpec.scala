package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import Encodings._

class EncodingSpec extends WordSpec with MustMatchers {

  "Encodings:  Should produce a encoding" in {
    Encodings.parseEncodings("compress") must equal (List(compress))
  }

  "Encodings:  Should produce list of encodings" in {
    Encodings.parseEncodings("x-compress, *") must equal (List(`x-compress`, `*`))
  }

  "Encodings:  Should produce custom encodings" in {
    Encodings.parseEncodings("customa, customb") must equal (List(CustomEncoding("customa"), CustomEncoding("customb")))
  }
}

