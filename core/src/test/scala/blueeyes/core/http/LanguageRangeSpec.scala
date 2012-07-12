package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class LanguageRangeSpec extends WordSpec with MustMatchers {

  "Language-Range:  Should produce en-uk from \"en-uk\"" in {
    LanguageRanges.parseLanguageRanges("en-uk")(0).value must equal ("en-uk")
  }

  "Accept-Language:  Should create languages (en-us-calif, is, cn) from \"en-us-calif, is=now!, cn; q=1\""  in {
    HttpHeaders.`Accept-Language`(LanguageRanges.parseLanguageRanges("en-us-calif, is=now!, cn; q=1"): _*)
      .value must equal ("en-us-calif, is, cn")
  }

}
