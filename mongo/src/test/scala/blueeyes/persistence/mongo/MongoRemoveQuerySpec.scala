package blueeyes.persistence.mongo

import org.scalatest._
import MongoQueryBuilder._
import MongoFilterOperators._

class MongoRemoveQuerySpec extends WordSpec with MustMatchers {

  "'where' method sets new filter" in {
    val query = remove.from("collection")
    query.where("name" === "Joe") must equal ( MongoRemoveQuery("collection", Some(MongoFieldFilter("name", $eq, "Joe"))) )
  }
}
