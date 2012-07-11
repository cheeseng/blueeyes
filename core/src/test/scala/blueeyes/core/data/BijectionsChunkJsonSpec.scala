package blueeyes.core.data

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import blueeyes.json.JsonAST._
import blueeyes.json.JsonParser.ParseException

class BijectionsChunkJsonSpec extends WordSpec with MustMatchers with BijectionsChunkJson{
  "BijectionsChunkJson" should{
    "parser valid JSON" in{
      JValueToChunk.unapply(Chunk("""{"foo": "bar"}""".getBytes())) must equal (JObject(List(JField("foo", JString("bar")))))
    }
    "throw error when JSON is incomplete" in{
      intercept[ParseException] {
        JValueToChunk.unapply(Chunk("""{"foo": "bar""".getBytes()))
      }
    }
  }
}
