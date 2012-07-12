package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class ExpectationSpec extends WordSpec with MustMatchers {

  "Expectation:  Should return continue or failure" in {
    HttpHeaders.Expect(Expectations.parseExpectations("100").get).value must equal ("100-continue")
  }

  "Expectation: Should return failure" in {
    HttpHeaders.Expect(Expectations.parseExpectations("417").get).value must equal ("417-expectationfailed")
  }

  "Expectation: Should parse to none on bad input" in { 
    Expectations.parseExpectations("asdf4s17") must equal (None)
  }
}

