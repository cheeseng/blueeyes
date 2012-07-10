package blueeyes.core.data

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import blueeyes.json.JsonAST._
import blueeyes.concurrent.test.AkkaFutures
import akka.dispatch.Future

class BijectionsChunkByteArraySpec extends WordSpec with MustMatchers with BijectionsByteArray with BijectionsChunkByteArray with blueeyes.bkka.AkkaDefaults with AkkaFutures {
  private val jObject1 = JObject(List(JField("foo", JString("bar"))))
  private val jObject2 = JObject(List(JField("bar", JString("foo"))))
  private val bijection = chunksToChunksArrayByte[JValue]

  "BijectionsChunkByteArray" should{
    "convert chunk to bytes chunks" in{
      val chunks     = Chunk(jObject1, Some(Future[Chunk[JValue]](Chunk(jObject2))))
      val bytesChunk = bijection(chunks)

      ByteArrayToJValue(bytesChunk.data) must equal (jObject1)

      whenReady(bytesChunk.next.get) { chunk => 
        ByteArrayToJValue(chunk.data) must equal (jObject2)
      }
    }

    "convert bytes chunk to chunk" in{
      val chunks     = Chunk(JValueToByteArray(jObject1), Some(Future[ByteChunk](Chunk(JValueToByteArray(jObject2)))))
      val bytesChunk = bijection.unapply(chunks)

      bytesChunk.data must equal (jObject1)
      whenReady(bytesChunk.next.get.map(_.data)) { result => 
        result must be (jObject2)
      }
    }
  }
}
