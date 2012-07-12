package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class PragmaDirectiveSpec extends WordSpec with MustMatchers {

  "Pragma: Parsing should return 'no-cache'"  in {
    HttpHeaders.Pragma(PragmaDirectives.parsePragmaDirectives(" No-Cache ").get).value must equal ("no-cache")
  }

  "Pragma: Parsing should return None on bad input" in {
    PragmaDirectives.parsePragmaDirectives(" zom ") must equal (None)
  }
}
