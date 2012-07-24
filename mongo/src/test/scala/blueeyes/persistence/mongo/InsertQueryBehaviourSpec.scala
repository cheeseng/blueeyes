package blueeyes.persistence.mongo

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import MongoQueryBuilder._
import blueeyes.json.JsonAST._
import org.mockito.Mockito._

class InsertQueryBehaviourSpec extends WordSpec with MustMatchers with MockitoSugar {
  private val jObject = JObject(JField("address", JObject( JField("city", JString("London")) :: JField("street", JString("Regents Park Road")) ::  Nil)) :: Nil)
  "Call collection method" in{
    val collection  = mock[DatabaseCollection]
    when(collection.getLastError) thenReturn None

    val query  = insert(jObject).into("collection")
    query(collection)

    verify(collection, times(1)).insert(jObject :: Nil)
  }
}
