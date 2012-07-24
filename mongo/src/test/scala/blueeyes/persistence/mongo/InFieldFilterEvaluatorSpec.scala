package blueeyes.persistence.mongo

import org.scalatest._
import blueeyes.json.JsonAST._
import Evaluators._

class InFieldFilterEvaluatorSpec extends WordSpec with MustMatchers {
  "returns true when value in array" in {
    InFieldFilterEvaluator(JString("b"), JArray(JString("b") :: JString("a") :: Nil)) must equal (true)
  }
  "returns false when value in not array" in {
    InFieldFilterEvaluator(JString("b"), JArray(JString("c") :: JString("a") :: Nil)) must equal (false)
  }
}
