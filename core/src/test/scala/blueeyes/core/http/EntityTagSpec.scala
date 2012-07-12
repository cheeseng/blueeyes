package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import blueeyes.core.http.MimeTypes._

class EntityTagSpec extends WordSpec with MustMatchers {

  /* Some more work should be done on entity tags */
  "If-Match:  Should return strings on well-formed input" in {
    HttpHeaders.`If-Match`(EntityTags.parseEntityTags("\"c4tattack\", \"cyberTiger\"").get).value must equal ("\"c4tattack\", \"cybertiger\"")
    }

  "If-Match:  Should return * string on presence of *; also, parser" in {
    HttpHeaders.`If-Match`(EntityTags.parseEntityTags("*, \"c4tattack\", \"cyberTiger\"").get).value must equal ("*")
  }

  "If-Match: Should return none text not enclosed with quotes" in {
    EntityTags.parseEntityTags("w%015") must equal (None)
  }

}
