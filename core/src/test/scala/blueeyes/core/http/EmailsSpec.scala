package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class EmailsSpec extends WordSpec with MustMatchers {
  "Emails" should{
    "return the correct email name with a well-formed email" in {
      Emails("johnsmith@socialmedia.com ") must be (Some(Email("johnsmith@socialmedia.com")))
    }

    "return the correct (although weird) email" in {
      Emails(" j.o.n.Sm.ith@so.cia.lmedia.com ") must be (Some(Email("j.o.n.Sm.ith@so.cia.lmedia.com")))
    }

    "parse non-email to None" in {
      Emails("209h3094)(it092jom") must equal (None)
    }
  }

}
