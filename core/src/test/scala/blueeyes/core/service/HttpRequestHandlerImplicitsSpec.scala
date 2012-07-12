package blueeyes.core.service

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class HttpRequestHandlerImplicitsSpec extends WordSpec with MustMatchers with HttpRequestHandlerImplicits{
  "HttpRequestHandlerImplicits.identifierToIdentifierWithDefault: creates IdentifierWithDefault" in {
    import HttpRequestHandlerImplicits._
    val identifierWithDefault = 'foo ?: "bar"
    identifierWithDefault.default must equal (Some("bar"))
  }
}

