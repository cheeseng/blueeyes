package blueeyes.persistence.mongo

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import blueeyes.json.JsonAST._
import com.mongodb.MongoException
import org.mockito.Mockito._
import blueeyes.concurrent.test.AkkaFutures

class MongoQueryBehaviourSpec extends WordSpec with MustMatchers with MockitoSugar with AkkaFutures {
  private object verifiableQuery extends QueryBehaviours.MongoQueryBehaviour {
    val isVerifiable = true
    type QueryResult = Int
    def query(collection: DatabaseCollection): Int = 1
  }

  private object unverifiableQuery extends QueryBehaviours.MongoQueryBehaviour {
    val isVerifiable = false
    type QueryResult = Int
    def query(collection: DatabaseCollection): Int = 1
  }

  "MongoQueryBehaviourSpec: calls underlying verifiable query" in{
    val collection  = mock[DatabaseCollection]

    when(collection.getLastError) thenReturn None

    val result: Int    = verifiableQuery(collection)

    verify(collection, times(1)).requestStart
    verify(collection, times(1)).getLastError
    verify(collection, times(1)).requestDone

    result.value must equal (1)
  }

  "MongoQueryBehaviourSpec: calls underlying unverifiable query" in{
    val collection  = mock[DatabaseCollection]

    when(collection.getLastError) thenReturn None

    val result: Int    = unverifiableQuery(collection)

    verify(collection, times(0)).requestStart
    verify(collection, times(0)).getLastError
    verify(collection, times(0)).requestDone

    result.value must equal (1)
  }

  "MongoQueryBehaviourSpec: throw error when verifiable operation failed" in{
    val collection  = mock[DatabaseCollection]

    when(collection.getLastError) thenReturn Some(new com.mongodb.BasicDBObject())

    intercept[MongoException] { verifiableQuery(collection) }

    verify(collection, times(1)).requestStart
    verify(collection, times(1)).getLastError
    verify(collection, times(1)).requestDone
  }
}
