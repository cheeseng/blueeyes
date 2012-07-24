package blueeyes.persistence.mongo

import org.scalatest._
import MongoQueryBuilder._
import MongoFilterBuilder._
import MongoFilterOperators._
import blueeyes.json.JPathImplicits._
import blueeyes.json.JPath

class MongoMapReduceQuerySpec extends WordSpec with MustMatchers {
  private val query = mapReduce("foo", "bar").from(MongoCollectionReference("collection"))

  "'where' method sets new filter" in {
    query.where("name" === "Joe") must equal (MongoMapReduceQuery("foo", "bar",  "collection", None, Some(MongoFieldFilter("name", $eq, "Joe"))))
  }

  "'on' method set output collection name" in {
    query.into("output") must equal (MongoMapReduceQuery("foo", "bar",  "collection", Some("output"), None))
  }
}
