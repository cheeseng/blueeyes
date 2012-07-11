package blueeyes.core.data

import akka.dispatch.Future
import blueeyes.bkka.AkkaDefaults
import blueeyes.concurrent.test.AkkaFutures

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import java.util.zip.{Inflater, InflaterInputStream}

class ZLIBByteChunkSpec extends WordSpec with MustMatchers with AkkaFutures with AkkaDefaults {
  "GZICompressedByteChunk" should{
    "compress one chunk" in{
      testCompressed(Chunk("foo".getBytes), "foo")
    }
    "compress several chunks" in{
      testCompressed(Chunk("foo".getBytes, Some(Future(Chunk("bar".getBytes)))), "foobar")
    }
  }

  private def testCompressed(chunk: ByteChunk, data: String) = {
    val compressed = ZLIBByteChunk(chunk)
    val future     = AggregatedByteChunk(compressed, None)

    future.map(v => new String(decompress(v))).futureValue must equal (data)
    //future.map(v => new String(decompress(v))) must whenDelivered { be_==(data) }
  }

  private def decompress(chunk: ByteChunk) = {
    val in  = new InflaterInputStream(new ByteArrayInputStream(chunk.data), new Inflater())
    val out = new ByteArrayOutputStream()
    val buf = new Array[Byte](1024)
    var len = in.read(buf)

    while (len > 0)
    do{
      out.write(buf, 0, len)
      len = in.read(buf)
    } while (len > 0)

    in.close()
    out.close()

    out.toByteArray
  }

}
