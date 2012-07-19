package blueeyes.util

import org.scalatest._

class SpecialCharTranscoderSpec extends WordSpec with MustMatchers {
  val transcoder = SpecialCharTranscoder.fromMap('_', Map('.' -> 'd', '@' -> 'a'))
  
  "SpecialCharTranscoder.encode" should {
    "encode specified chars" in { 
      transcoder.encode("@foo.baz") must equal ("_afoo_dbaz")
    }
    
    "encode escape char" in {
      transcoder.encode("_@_") must equal ("___a__")
    }
  }
  
  "SpecialCharTranscoder.decode" should {
    "decode specified chars" in { 
      transcoder.decode("_afoo_dbaz") must equal ("@foo.baz")
    }
    
    "decode escape char" in {
      transcoder.decode("___a__") must equal ("_@_")
    }
  }
}
