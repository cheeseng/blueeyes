package blueeyes.persistence.mongo

import org.scalatest._
import blueeyes.json.JsonAST._
import Evaluators._


class GtFieldFilterEvaluatorSpec extends WordSpec with MustMatchers {
  "returns true when one string greater then another string" in {
    GtFieldFilterEvaluator(JString("b"), JString("a")) must equal (true)
  }
  "returns false when one string less then another string" in {
    GtFieldFilterEvaluator(JString("a"), JString("b")) must equal (false)
  }
  "returns true when one number greater then another number" in {
    GtFieldFilterEvaluator(JInt(2), JInt(1)) must equal (true)
  }
  "returns false when one number less then another number" in {
    GtFieldFilterEvaluator(JInt(1), JInt(2)) must equal (false)
  }
  "returns true when one double greater then another double" in {
    GtFieldFilterEvaluator(JDouble(2.2), JDouble(1.1)) must equal (true)
  }
  "returns false when one double less then another double" in {
    GtFieldFilterEvaluator(JDouble(1.1), JDouble(2.2)) must equal (false)
  }
  "returns true when one boolean greater then another boolean" in {
    GtFieldFilterEvaluator(JBool(true), JBool(false)) must equal (true)
  }
  "returns false when one boolean less then another boolean" in {
    GtFieldFilterEvaluator(JBool(false), JBool(true)) must equal (false)
  }
  "returns false when different object are compared" in {
    GtFieldFilterEvaluator(JBool(false), JInt(1)) must equal (false)
  }
  "returns false when objecta are the same" in {
    GtFieldFilterEvaluator(JInt(1), JInt(1)) must equal (false)
  }
  
}
