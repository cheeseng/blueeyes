package blueeyes.persistence.mongo

import org.scalacheck._
import Gen._
import Arbitrary.arbitrary
import org.scalacheck.Prop._

import blueeyes.json.JsonAST._
import blueeyes.json._
import MongoFilterImplicits._
import org.scalatest._
import org.scalatest.prop.Checkers

class MongoAndFilterSpec extends WordSpec with MustMatchers with Checkers with MongoImplicits with ArbitraryJValue with ArbitraryMongo{
  private val filter1    = MongoFilterBuilder(JPath("foo")).>(MongoPrimitiveInt(1))
  private val filter2    = MongoFilterBuilder(JPath("bar")).<(MongoPrimitiveInt(5))
  private val filter3    = MongoFilterBuilder(JPath("rar")).<(MongoPrimitiveInt(6))
  private val filter4    = MongoFilterBuilder(JPath("baz")).<(MongoPrimitiveInt(6))
  private val andFilter  = filter1 && filter2

  def getDifferentOrdersAnds: Gen[(MongoAndFilter, MongoAndFilter)] = getListMongoFieldFilter.map{filters =>
    def andFilter(values: List[MongoFieldFilter]) = values.tail.foldLeft(MongoAndFilter(List(values.head))){(andFilter, filter) => andFilter && filter}
    (andFilter(filters), andFilter(filters.reverse))
  }

  implicit def arbDifferentOrdersAnds: Arbitrary[(MongoAndFilter, MongoAndFilter)] = Arbitrary(getDifferentOrdersAnds)

  "MongoAndFilter" should{
    "convert to the same JValue, no matter the order of constructions" in{
      check { filters: (MongoAndFilter, MongoAndFilter) => filters._1.filter == filters._2.filter }
    }

    "should equal, no matter the order of constructions" in{
      check { filters: (MongoAndFilter, MongoAndFilter) => filters._1 == filters._2 }
    }

    "should have the same hashCodes, no matter the order of constructions" in{
      check { filters: (MongoAndFilter, MongoAndFilter) => filters._1.hashCode == filters._1.hashCode }
    }

    "create valid json for or filter" in {
      (andFilter).filter must equal (JObject(filter1.filter.asInstanceOf[JObject].fields ++ filter2.filter.asInstanceOf[JObject].fields))
      (andFilter && filter3).filter must equal (JObject(filter1.filter.asInstanceOf[JObject].fields ++ filter2.filter.asInstanceOf[JObject].fields ++ filter3.filter.asInstanceOf[JObject].fields))
      (filter3 && andFilter).filter must equal (JObject(filter3.filter.asInstanceOf[JObject].fields ++ filter2.filter.asInstanceOf[JObject].fields ++ filter1.filter.asInstanceOf[JObject].fields))
      (andFilter && (filter3 && filter4)).filter must equal (JObject(filter1.filter.asInstanceOf[JObject].fields ++ filter2.filter.asInstanceOf[JObject].fields ++ filter3.filter.asInstanceOf[JObject].fields ++ filter4.filter.asInstanceOf[JObject].fields))
    }
    "create valid json for AND filter with $eq filter" in {
      (("foo" === 1) && ("foo" !== 2)).filter must equal (JObject(JField("foo", JInt(1)) :: JField("foo", JObject(JField("$ne", JInt(2)) :: Nil)) :: Nil))
    }

    "combine with filter3 to create a new filter" in {
      (andFilter && filter3).filter must equal (JObject(filter1.filter.asInstanceOf[JObject].fields ++
        filter2.filter.asInstanceOf[JObject].fields ++
        filter3.filter.asInstanceOf[JObject].fields))
    }

    "combine ANDs with ORs" in {
      val exam: MongoFilter = ("address.city" === "B") ||  ("address.street" === "2") || ("address.code" === 1)
      val cfilter: MongoFilter = ((filter1 && filter2 && filter3) || (filter2 && filter3) || (filter1 && filter3)) //|| (filter1 && filter2)
      cfilter.filter must equal (JsonParser.parse("""
         {
        "$or":[{
          "foo":{
            "$gt":1
          },
          "bar":{
            "$lt":5
          },
          "rar":{
            "$lt":6
          }
        },{
          "bar":{
            "$lt":5
          },
          "rar":{
            "$lt":6
          }
        },{
          "foo":{
            "$gt":1
          },
          "rar":{
            "$lt":6
          }
        }]
      }
    """))
    }

    "unary_! use 'or' use with negative operators of subfilters " in{
      (andFilter).unary_! must equal (filter1.unary_! || filter2.unary_!)
    }

    "2 unary_! result to the same filter" in{
      (andFilter).unary_!.unary_! must equal (andFilter)
    }

    "and with MongoFilterAll return filter" in{
      (MongoFilterAll && filter2).filter must equal (filter2.filter)
      (filter2 && MongoFilterAll).filter must equal (filter2.filter)
    }
  }
}
