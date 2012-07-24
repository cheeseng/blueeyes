package blueeyes.persistence.mongo

import org.scalatest._
import MongoFilterOperators._

class MongoFilterOperatorSpec extends WordSpec with MustMatchers {
  "$gt opposite operator is $lte" in {
    $gt.unary_! must be ($lte)
  }
  "$gte opposite operator is $lt" in {
    $gte.unary_! must be ($lt)
  }
  "$lt opposite operator is $gte" in {
    $lt.unary_! must be ($gte)
  }
  "$lte opposite operator is $gt" in {
    $lte.unary_! must be ($gt)
  }
  "$eq opposite operator is $ne" in {
    $eq.unary_! must be ($ne)
  }
  "$in opposite operator is $nin" in {
    $in.unary_! must be ($nin)
  }
  "$mod does not have a negation" in {
    intercept[java.lang.RuntimeException] { $mod.unary_! }
  }
  "$all does not have a negation" in {
    intercept[java.lang.RuntimeException] { $all.unary_! } 
  }
  "$size does not have a negation" in {
    intercept[java.lang.RuntimeException] { $size.unary_! } 
  }
  "$type does not have a negation" in {
    intercept[java.lang.RuntimeException] { $type.unary_! } 
  }
  "$or does not have a negation" in {
    intercept[java.lang.RuntimeException] { $or.unary_! }
  }
}
