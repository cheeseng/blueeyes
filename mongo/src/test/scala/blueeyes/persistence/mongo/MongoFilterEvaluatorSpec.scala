package blueeyes.persistence.mongo

import org.scalatest._
import blueeyes.json.JsonAST._
import blueeyes.persistence.mongo.MongoFilterOperators._
import blueeyes.json.JsonParser

class MongoFilterEvaluatorSpec extends WordSpec with MustMatchers {
  private val jObject  = JObject(JField("address", JObject( JField("city", JString("A")) :: JField("street", JString("1")) ::  Nil)) :: Nil)
  private val jObject1 = JObject(JField("address", JObject( JField("city", JString("B")) :: JField("street", JString("2")) ::  Nil)) :: Nil)
  private val jObject2 = JObject(JField("address", JObject( JField("city", JString("B")) :: JField("street", JString("3")) ::  Nil)) :: Nil)
  private val jObject3 = JObject(JField("address", JObject( JField("city", JString("C")) :: JField("street", JString("4")) ::  Nil)) :: Nil)
  private val jobjects = jObject :: jObject1 :: jObject2 :: jObject3 :: Nil

  private val jobjectsWithArray = JsonParser.parse("""{ "foo" : [
      {
        "shape" : "square",
        "color" : "purple",
        "thick" : false
      },
      {
        "shape" : "circle",
        "color" : "red",
        "thick" : true
      }
] } """) :: JsonParser.parse("""
{ "foo" : [
      {
        "shape" : "square",
        "color" : "red",
        "thick" : true
      },
      {
        "shape" : "circle",
        "color" : "green",
        "thick" : false
      }
] }""") :: Nil


  "MongoFilterEvaluator" should{
    "selects all objects by MongoFilterAll" in{
      MongoFilterEvaluator(jobjects).filter(MongoFilterAll) must equal (jobjects)
    }
    "selects objects by field query" in{
      MongoFilterEvaluator(jobjects).filter(MongoFieldFilter("address.city", $eq,"B")) must equal (jObject1 :: jObject2 :: Nil)
    }
    "selects objects by 'or' query" in{
      val result = jObject1 :: jObject2 :: jObject3 :: Nil
      MongoFilterEvaluator(jobjects).filter(MongoFieldFilter("address.city", $eq,"B") || MongoFieldFilter("street.street", $eq,"4")).filterNot (result contains) must equal (Nil)
    }
    "selects objects by 'and' query" in{
      val result = jObject2 :: Nil
      MongoFilterEvaluator(jobjects).filter(MongoFieldFilter("address.city", $eq,"B") && MongoFieldFilter("street.street", $eq,"3")).filterNot (result contains) must equal (Nil)
    }

    "select object using elemeMatch" in {
       MongoFilterEvaluator(jobjectsWithArray).filter(MongoElementsMatchFilter("foo", (MongoFieldFilter("shape", $eq,"square") && MongoFieldFilter("color", $eq,"purple")))) must equal (jobjectsWithArray.head :: Nil)
    }
    "does not select object using elemeMatch and wrong query" in {
       MongoFilterEvaluator(jobjectsWithArray).filter(MongoElementsMatchFilter("foo", (MongoFieldFilter("shape", $eq,"square") && MongoFieldFilter("color", $eq,"freen")))) must equal (Nil)
    }
    "select element from array" in {
       MongoFilterEvaluator(JsonParser.parse("[1, 2]").asInstanceOf[JArray].elements).filter(MongoFieldFilter("", $eq, 1)) must equal (JInt(1) :: Nil)
    }

    "select element by complex filter " in {
       MongoFilterEvaluator(JsonParser.parse("""[{"foo": 1}, {"foo": 2}]""").asInstanceOf[JArray].elements).filter(MongoFieldFilter("foo", $eq, 1)) must equal (JsonParser.parse("""{"foo": 1}""") :: Nil)
    }
    "select element from array by element match " in {
       MongoFilterEvaluator(JsonParser.parse("""[{"foo": 1}, {"foo": 2}]""").asInstanceOf[JArray].elements).filter(MongoAndFilter(List(MongoFieldFilter("foo", $eq, 1))).elemMatch("")) must equal (JsonParser.parse("""{"foo": 1}""") :: Nil)
    }
  }
}
