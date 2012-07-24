package blueeyes.persistence.mongo

import org.scalatest._
import MongoFilterOperators._
import blueeyes.json.JsonAST._
import blueeyes.json.JPathImplicits._
import blueeyes.json._

class MongoElementsMatchFilterSpec extends WordSpec with MustMatchers {
  private val filter1    = MongoFilterBuilder(JPath("foo")).>(MongoPrimitiveInt(1))
  private val filter2    = MongoFilterBuilder(JPath("bar")).<(MongoPrimitiveInt(5))
  private val andFilter  = filter1 && filter2

  "create valid json for or filter" in {
    (andFilter.elemMatch("name")).filter must equal (JsonParser.parse(""" {"name": {"$elemMatch" : {"foo": {"$gt": 1}, "bar": {"$lt": 5}} }} """))
  }
  "create valid json for or filter when path is empty" in {
    (andFilter.elemMatch("")).filter must equal (JsonParser.parse(""" {"$elemMatch" : {"foo": {"$gt": 1}, "bar": {"$lt": 5}} } """))
  }
}
