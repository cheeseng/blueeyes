package blueeyes.persistence.mongo

import org.scalatest._
import MongoQueryBuilder._
import MongoFilterOperators._

class MongoCountQuerySpec extends WordSpec with MustMatchers{
  private val query = count.from(MongoCollectionReference("collection"))

  "'where' method sets new filter" in {
    query.where("name" === "Joe") must equal ( MongoCountQuery("collection", Some(MongoFieldFilter("name", $eq, "Joe"))) )
  }
}
