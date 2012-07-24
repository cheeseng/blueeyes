package blueeyes.persistence.mongo

import org.scalatest._
import UpdateFieldFunctions._

class IncFSpec extends WordSpec with MustMatchers {
  "fuse increases set update" in {
    IncF("n", 2).fuseWith(SetF("n", 3)) must equal (Some(SetF("n", 5)))
  }
  "fuse increases set inc update" in {
    IncF("n", 2).fuseWith(IncF("n", 4)) must equal (Some(IncF("n", 6)))
  }
}
