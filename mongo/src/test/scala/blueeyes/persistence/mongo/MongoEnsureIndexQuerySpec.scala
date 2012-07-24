package blueeyes.persistence.mongo

import org.scalatest._
import MongoFilterOperators._
import blueeyes.json.JPath
import blueeyes.json.JPathImplicits._

class MongoEnsureIndexQuerySpec extends WordSpec with MustMatchers with MongoImplicits{
  private val query = ensureIndex("foo").on("bar").in("collection")

  "'geospatial' method sets geospatial index type" in {
    query.geospatial("bar") must equal (MongoEnsureIndexQuery("collection", "foo", List(Tuple2(JPath("bar"), GeospatialIndex)), false))
  }
}
