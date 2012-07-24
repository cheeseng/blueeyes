package blueeyes.persistence.mongo

import org.scalatest._
import MongoUpdateOperators._
import MongoFilterOperators._
import blueeyes.json.JsonAST._
import blueeyes.json.JPath

class MongoUpdateBuilderSpec extends WordSpec with MustMatchers {
  "builds $inc operation" in {
    ("n" inc (1) toJValue) must equal (JObject(JField("$inc", JObject(JField("n", JInt(1)) :: Nil)) :: Nil))
  }
  "builds $set operation" in {
    ("n" set (1) toJValue) must equal (JObject(JField("$set", JObject(JField("n", JInt(1)) :: Nil)) :: Nil))
  }
  "builds $unset operation" in {
    (("n" unset) toJValue) must equal (JObject(JField("$unset", JObject(JField("n", JInt(1)) :: Nil)) :: Nil))
  }
  "builds popLast operation" in {
    (("n" popLast) toJValue) must equal (JObject(JField("$pop", JObject(JField("n", JInt(1)) :: Nil)) :: Nil))
  }
  "builds popFirst operation" in {
    (("n" popFirst) toJValue) must equal (JObject(JField("$pop", JObject(JField("n", JInt(-1)) :: Nil)) :: Nil))
  }

  "builds $push operation" in {
    ("n" push (1) toJValue) must equal (JObject(JField("$push", JObject(JField("n", JInt(1)) :: Nil)) :: Nil))
  }
  "builds $pull operation with default operator" in {
    (("n" pull(JPath("") === 1)).toJValue) must equal (JObject(JField("$pull", JObject(JField("n", JInt(1)) :: Nil)) :: Nil))
  }
  "builds $pull operation with custom operator" in {
    (("n" pull(JPath("") > 1)).toJValue) must equal (JObject(JField("$pull", JObject(JField("n", JObject(JField("$gt", JInt(1)) :: Nil)) :: Nil)) :: Nil))
  }
  "builds $pullAll operation" in {
    ("n" pullAll (MongoPrimitiveString("foo"), MongoPrimitiveString("bar")) toJValue) must equal (JObject(JField("$pullAll", JObject(JField("n", JArray(JString("foo") :: JString("bar") :: Nil)) :: Nil)) :: Nil))
  }
  "builds $addToSet operation for one element" in {
    ("n" addToSet (MongoPrimitiveString("foo")) toJValue) must equal (JObject(JField("$addToSet", JObject(JField("n", JString("foo")) :: Nil)) :: Nil))
  }
  "builds $addToSet operation for several element" in {
    ("n" addToSet (MongoPrimitiveString("foo"), MongoPrimitiveString("bar")) toJValue) must equal (JObject(JField("$addToSet", JObject(JField("n", JObject(JField("$each", JArray(JString("foo") :: JString("bar") :: Nil)) :: Nil)) :: Nil)) :: Nil))
  }
}
