package blueeyes.core.data

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import akka.dispatch.Future
import blueeyes.bkka.AkkaDefaults
import blueeyes.util.metrics.DataSize
import blueeyes.concurrent.test.AkkaFutures
import DataSize._

class AggregatedByteChunkSpec extends WordSpec with MustMatchers with AkkaDefaults with AkkaFutures {
  "AggregatedByteChunk" should {
    "aggregate full content when size is not specified" in{
      val chunk = Chunk(Array[Byte]('1', '2'), Some(Future(Chunk(Array[Byte]('3', '4')))))
      whenReady(AggregatedByteChunk(chunk, None).map(v => new String(v.data))) { result => 
        result must be ("1234")
      }
    }
    "aggregate content up to the specified size" in{
      val chunk = Chunk(Array[Byte]('1', '2'), Some(Future(Chunk(Array[Byte]('3', '4')))))
      whenReady(AggregatedByteChunk(chunk, Some(2.bytes)).map(v => new String(v.data))) { result => 
        result must be ("12")
      }
    }
  }
}
