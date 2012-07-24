package blueeyes.persistence.mongo

import org.scalatest._
import blueeyes.json.JsonAST._
import blueeyes.json.{JPath, JsonParser}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

class ExplainQueryBehaviourSpec extends WordSpec with MustMatchers with MockitoSugar {
  private val explanation = JsonParser.parse("""{
    "cursor" : "BasicCursor",
    "nscanned" : 3,
    "nscannedObjects" : 3,
    "n" : 3,
    "millis" : 38,
    "nYields" : 0,
    "nChunkSkips" : 0,
    "isMultiKey" : false,
    "indexOnly" : false,
    "indexBounds" : {

    }
}""").asInstanceOf[JObject]

  private val keys     = MongoSelection(Set(JPath("foo"), JPath("bar")))

  "Call collection method" in{
    val collection  = mock[DatabaseCollection]
    when(collection.getLastError) thenReturn None
    when(collection.explain(keys, None, None, None, None, None, false)) thenReturn explanation

    val query  = select("foo", "bar").from("collection").explain
    val result: JObject = query(collection)

    verify(collection, times(1)).explain(keys, None, None, None, None, None, false)
    result must equal (explanation)
  }

}
