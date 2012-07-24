package blueeyes.persistence.mongo

import org.scalatest._
import MongoFilterOperators._
import blueeyes.json.JPath
import blueeyes.json.JsonAST._
import MongoFilterImplicits._

class MongoFilterBuilderSpec extends WordSpec with MustMatchers {

  "builds $eq operation" in {
    JPath("foo") === "bar" must equal (MongoFieldFilter("foo", $eq, MongoPrimitiveString("bar")))
  }
  "builds $ne operation" in {
    (JPath("foo") !== 1) must equal (MongoFieldFilter("foo", $ne, MongoPrimitiveInt(1)))
  }
  "builds $gt operation" in {
    JPath("foo") > 1l must equal (MongoFieldFilter("foo", $gt, MongoPrimitiveLong(1l)))
  }
  "builds $gte operation" in {
    JPath("foo") >= 1.1 must equal (MongoFieldFilter("foo", $gte, MongoPrimitiveDouble(1.1)))
  }
  "builds $lte operation" in {
    JPath("foo") <= 1.1 must equal (MongoFieldFilter("foo", $lte, MongoPrimitiveDouble(1.1)))
  }
  "builds $in operation" in {
    JPath("foo").anyOf(MongoPrimitiveString("foo"), MongoPrimitiveString("bar")) must equal (MongoFieldFilter("foo", $in, MongoPrimitiveArray(MongoPrimitiveString("foo") :: MongoPrimitiveString("bar") :: Nil)))
  }
  "builds $all operation" in {
    JPath("foo").contains(MongoPrimitiveString("foo"), MongoPrimitiveString("bar")) must equal (MongoFieldFilter("foo", $all, MongoPrimitiveArray(MongoPrimitiveString("foo") :: MongoPrimitiveString("bar") :: Nil)))
  }

  "builds $size operation" in {
    JPath("foo").hasSize(1) must equal (MongoFieldFilter("foo", $size, MongoPrimitiveInt(1)))
  }
  "builds $exists operation" in {
    MongoFilterBuilder(JPath("foo")).isDefined must equal (MongoFieldFilter("foo", $exists, MongoPrimitiveBoolean(true)))
  }
  "builds $hasType operation" in {
    JPath("foo").hasType[JString] must equal (MongoFieldFilter("foo", $type, MongoPrimitiveInt(2)))
  }
  "builds $regex operation" in {
    ("foo" regex "bar") must equal (MongoFieldFilter("foo", $regex, MongoPrimitiveJObject(JObject(List(JField("$regex", JString("bar")), JField("$options", JString("")))))))
  }
  "builds $regex operation with options" in {
    ("foo" regex ("bar", "i")) must equal (MongoFieldFilter("foo", $regex, MongoPrimitiveJObject(JObject(List(JField("$regex", JString("bar")), JField("$options", JString("i")))))))
  }
  "builds $near" in {
    ("foo" near (50, 60)) must equal (MongoFieldFilter("foo", $near, MongoPrimitiveJObject(JObject(List(JField("$near", JArray(List(JDouble(50.0), JDouble(60.0)))))))))
  }
  "builds $within for box" in {
    ("foo" within Box((10, 20), (30, 40))) must equal (MongoFieldFilter("foo", $within, MongoPrimitiveJObject(JObject(JField("$within", JObject(JField("$box", JArray(JArray(JDouble(10.0) :: JDouble(20.0) :: Nil) :: JArray(JDouble(30.0) :: JDouble(40.0) :: Nil) :: Nil)) :: Nil)) :: Nil))))
  }
  "builds $within for circe" in {
    ("foo" within Circle((10, 20), 30)) must equal (MongoFieldFilter("foo", $within, MongoPrimitiveJObject(JObject(JField("$within", JObject(JField("$center",  JArray(JArray(JDouble(10.0) :: JDouble(20.0) :: Nil) :: JDouble(30.0) :: Nil)) :: Nil)) :: Nil))))
  }
  "builds $within for CenterSphere" in {
    ("foo" within CenterSphere((10, 20), 0.3)) must equal (MongoFieldFilter("foo", $within, MongoPrimitiveJObject(JObject(JField("$within", JObject(JField("$centerSphere",  JArray(JArray(JDouble(10.0) :: JDouble(20.0) :: Nil) :: JDouble(0.3) :: Nil)) :: Nil)) :: Nil))))
  }
  "builds $within for polygon" in {
    ("foo" within Polygon((10, 20), (30, 40))) must equal (MongoFieldFilter("foo", $within, MongoPrimitiveJObject(JObject(JField("$within", JObject(JField("$polygon", JArray(JArray(JDouble(10.0) :: JDouble(20.0) :: Nil) :: JArray(JDouble(30.0) :: JDouble(40.0) :: Nil) :: Nil)) :: Nil)) :: Nil))))
  }
  "builds $where operation" in {
    (evaluation("this.a > 3")) must equal (MongoFieldFilter(JPath.Identity, $where, MongoPrimitiveString("this.a > 3")))
  }

}
