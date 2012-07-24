package blueeyes.persistence.mongo

import org.scalatest._
import MongoQueryBuilder._
import MongoFilterBuilder._
import MongoFilterOperators._
import blueeyes.json.JPathImplicits._
import blueeyes.json.JsonAST._
import blueeyes.json.JPath

class MongoSelectOneQuerySpec extends WordSpec with MustMatchers {
  private val query = selectOne("foo", "bar").from(MongoCollectionReference("collection"))

  "'where' method sets new filter" in {
    query.where("name".jpath === "Joe") must equal (MongoSelectOneQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection", Some(MongoFieldFilter("name", $eq, "Joe"))))
  }

  "'sortBy' method sets new sort" in {
    query.sortBy("name".jpath << ) must equal (MongoSelectOneQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection", None, Some(MongoSort(JPath("name"), MongoSortOrderDescending))))
  }
}
