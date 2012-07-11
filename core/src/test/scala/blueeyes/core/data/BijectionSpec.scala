package blueeyes.core.data

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class BijectionSpec extends WordSpec with MustMatchers {
  "Bijection.identity: creates Bijection which does not change data" in{
    Bijection.identity[String]("foo")         must equal ("foo")
    Bijection.identity[String].unapply("foo") must equal ("foo")
  }
  "Bijection.inverse: creates inverse Bijection" in{
    val inversed = BijectionsByteArray.ByteArrayToString.inverse
    inversed.unapply(Array[Byte]('f', 'o', 'o'))  must equal ("foo")
    inversed("foo").toList                        must equal (List[Byte]('f', 'o', 'o'))
  }
  "Bijection.compose: creates composed Bijection" in{
    val composed = BijectionsChunkString.StringToChunk.andThen(BijectionsChunkByteArray.ChunkToArrayByte)
    composed("foo").toList must equal (List[Byte]('f', 'o', 'o'))
  }
}
