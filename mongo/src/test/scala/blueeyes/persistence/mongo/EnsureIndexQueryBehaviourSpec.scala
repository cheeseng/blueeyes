package blueeyes.persistence.mongo

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import MongoQueryBuilder._
import org.mockito.Matchers._
import blueeyes.json.JsonAST._
import blueeyes.json.JPath
import org.mockito.Mockito._

class EnsureIndexQueryBehaviourSpec extends WordSpec with MustMatchers with MockitoSugar {
  "Call collection method" in {
    val collection  = mock[DatabaseCollection]
    when(collection.getLastError) thenReturn None

    val query = ensureUniqueIndex("index").on("address.city", "address.street").in("collection")
    query(collection)

    verify(collection, times(1)).ensureIndex("index", List(Tuple2(JPath("address.city"), OrdinaryIndex), Tuple2(JPath("address.street"), OrdinaryIndex)), true, JObject(Nil))
  }
}
