package blueeyes.persistence.mongo

import org.scalatest._
import MongoQueryBuilder._
import MongoFilterBuilder._
import MongoFilterOperators._
import blueeyes.json.JPath

class MongoDistinctQuerySpec extends WordSpec with MustMatchers{
  "'where' method sets new filter" in {
    val query = distinct("foo").from("collection")
    
    query.where("name" === "Joe") must equal (MongoDistinctQuery(JPath("foo"), "collection", Some(MongoFieldFilter("name", $eq, "Joe"))))
  }
}
