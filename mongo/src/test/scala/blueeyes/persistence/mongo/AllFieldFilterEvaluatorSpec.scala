package blueeyes.persistence.mongo

import org.scalatest._
import blueeyes.json.JsonAST._
import Evaluators._

class AllFieldFilterEvaluatorSpec  extends WordSpec with MustMatchers {
  "returns true when not all elemenets matched" in {
    AllFieldFilterEvaluator(JArray(JInt(2) :: JInt(3) :: Nil ), JArray(JInt(1) :: JInt(2) :: JInt(3) :: Nil )) must equal (true)
  }
  "returns false when all elemenets matched" in {
    AllFieldFilterEvaluator(JArray(JInt(2) :: JInt(3) :: JInt(4) :: Nil ), JArray(JInt(1) :: JInt(2) :: JInt(3) :: Nil )) must equal (false)
  }
}
