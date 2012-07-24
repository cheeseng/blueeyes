package blueeyes.persistence.mongo

import org.scalatest._
import blueeyes.json.{JPath, JPathIndex, JPathField}

class JPathExtensionSpec extends WordSpec with MustMatchers {
  "JPathExtension convert JPathIndex to JPathField" in {
    JPathExtension.toMongoField(JPath(JPathField("foo") :: JPathIndex(1) :: Nil)) must equal ("foo.1")
  }
}
