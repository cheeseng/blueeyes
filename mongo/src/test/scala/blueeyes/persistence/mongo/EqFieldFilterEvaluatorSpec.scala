package blueeyes.persistence.mongo

import org.scalatest._
import blueeyes.json.JsonAST._
import Evaluators._

class EqFieldFilterEvaluatorSpec  extends WordSpec with MustMatchers {
  "returns true for the same JValues" in {
    EqFieldFilterEvaluator(JString("foo"), JString("foo")) must equal (true)
  }
  "returns false for different JValues" in {
    EqFieldFilterEvaluator(JString("bar"), JString("foo")) must equal (false)
  }
  "returns true if valie is JNothing and matching value is JNull" in {
    EqFieldFilterEvaluator(JNothing, JNull) must equal (true)
  }
}
