package blueeyes.persistence.mongo

import org.scalatest._
import MongoQueryBuilder._
import MongoFilterBuilder._
import MongoFilterOperators._
import blueeyes.json.JsonAST._
import blueeyes.json.JPathImplicits._
import blueeyes.json.JPath

class MongoSelectQuerySpec extends WordSpec with MustMatchers with MongoImplicits{
  private val query = select("foo", "bar").from(MongoCollectionReference("collection"))

  "'where' method sets new filter" in {
    query.where("name".jpath === "Joe") must equal (MongoSelectQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection", Some(MongoFieldFilter("name", $eq, "Joe"))))
  }
  "'sortBy' method sets new sort" in {
    query.sortBy("name".jpath << ) must equal (MongoSelectQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection", None, Some(MongoSort(JPath("name"), MongoSortOrderDescending))))
  }
  "'skip' method sets new skip" in {
    query.skip(10) must equal (MongoSelectQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection", None, None, Some(10)))
  }
  "'snapshot' method sets snapshot mode" in {
    query.snapshot must equal (MongoSelectQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection", None, None, None, None, None, true))
  }
  "'limit' method sets new limit" in {
    query.limit(10) must equal (MongoSelectQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection", None, None, None, Some(10)))
  }
  "'hint' with name sets new hint" in {
    query.hint("foo") must equal (MongoSelectQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection", None, None, None, None, Some(NamedHint("foo"))))
  }
  "'hint' with keys sets new hint" in {
    query.hint(JPath("foo") :: JPath("bar") :: Nil) must equal (MongoSelectQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection", None, None, None, None, Some(KeyedHint(List(JPath("foo"), JPath("bar"))))))
  }
  "'explain' creates Explain query" in {
    query.hint(JPath("foo") :: JPath("bar") :: Nil).explain must equal (MongoExplainQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection", None, None, None, None, Some(KeyedHint(List(JPath("foo"), JPath("bar"))))))
  }
}
