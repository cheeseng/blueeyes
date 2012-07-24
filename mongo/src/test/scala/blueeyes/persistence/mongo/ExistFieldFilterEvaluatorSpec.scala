package blueeyes.persistence.mongo

import org.scalatest._
import blueeyes.json.JsonAST._
import Evaluators._

class ExistFieldFilterEvaluatorSpec  extends WordSpec with MustMatchers {

  "always returns true" in {
    ExistsFieldFilterEvaluator(JArray(JInt(2) :: JInt(3) :: Nil ), JBool(true)) must equal (true)
    ExistsFieldFilterEvaluator(JInt(4), JBool(true)) must equal (true)
  }
}
