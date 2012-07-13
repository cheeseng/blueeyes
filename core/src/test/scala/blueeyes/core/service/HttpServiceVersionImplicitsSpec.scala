package blueeyes.core.service

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class HttpServiceVersionImplicitsSpec extends WordSpec with MustMatchers {

  "HttpServiceVersionImplicits stringToVersion: creates version" in{
    ServiceVersion.fromString("1.2.3") must equal (ServiceVersion(1, 2, "3"))
  }
}
