package blueeyes.persistence.mongo

import org.scalatest._
import blueeyes.json.JsonAST._
import Evaluators._

class LteFieldFilterEvaluatorSpec extends WordSpec with MustMatchers {
  "returns false when one string greater then another string" in {
    LteFieldFilterEvaluator(JString("b"), JString("a")) must equal (false)
  }
  "returns true when one string less then another string" in {
    LteFieldFilterEvaluator(JString("a"), JString("b")) must equal (true)
  }
  "returns false when one number greater then another number" in {
    LteFieldFilterEvaluator(JInt(2), JInt(1)) must equal (false)
  }
  "returns true when one number less then another number" in {
    LteFieldFilterEvaluator(JInt(1), JInt(2)) must equal (true)
  }
  "returns false when one double greater then another double" in {
    LteFieldFilterEvaluator(JDouble(2.2), JDouble(1.1)) must equal (false)
  }
  "returns true when one double less then another double" in {
    LteFieldFilterEvaluator(JDouble(1.1), JDouble(2.2)) must equal (true)
  }
  "returns false when one boolean greater then another boolean" in {
    LteFieldFilterEvaluator(JBool(true), JBool(false)) must equal (false)
  }
  "returns true when one boolean less then another boolean" in {
    LteFieldFilterEvaluator(JBool(false), JBool(true)) must equal (true)
  }
  "returns false when different object are compared" in {
    LteFieldFilterEvaluator(JBool(false), JInt(1)) must equal (false)
  }
  "returns true when objecta are the same" in {
    LteFieldFilterEvaluator(JInt(1), JInt(1)) must equal (true)
  }  
}
