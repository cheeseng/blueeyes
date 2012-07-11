package blueeyes.core.data

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import Bijection._

class BijectionsChunkXMLSpec extends WordSpec with MustMatchers with BijectionsChunkXML with BijectionsByteArray{
  "BijectionsChunkXML" should{
    "parser valid XML" in{
      XMLToChunk.unapply(Chunk(XMLToByteArray(<f></f>))) must equal (<f></f>)
    }
    "throw error when XML is incomplete" in{
      intercept[RuntimeException] {
        XMLToChunk.unapply(Chunk((XMLToByteArray(<f></f>).tail)))
      }
    }
  }
}
