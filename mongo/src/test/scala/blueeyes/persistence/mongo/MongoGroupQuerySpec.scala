package blueeyes.persistence.mongo

import org.scalatest._
import MongoQueryBuilder._
import MongoFilterBuilder._
import MongoFilterOperators._
import blueeyes.json.JPath
import blueeyes.json.JPathImplicits._
import blueeyes.json.JsonAST.JObject

class MongoGroupQuerySpec extends WordSpec with MustMatchers {
  private val query = group(JObject(Nil), "dummy", "foo", "bar").from(MongoCollectionReference("collection"))

  "'where' method sets new filter" in {
    query.where("name" === "Joe") must equal (MongoGroupQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection", JObject(Nil), "dummy", Some(MongoFieldFilter("name", $eq, "Joe"))))
  }
}
