package blueeyes.core.service

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import akka.dispatch.Future
import blueeyes.core.http._
import blueeyes.core.http.test.HttpRequestCheckers
import blueeyes.core.data.{Chunk, ByteChunk}
import blueeyes.util.metrics.DataSize
import DataSize._
import blueeyes.core.http.HttpStatusCodes._

class HttpClientByteChunkSpec extends WordSpec with MustMatchers with blueeyes.bkka.AkkaDefaults with HttpRequestCheckers {
  "HttpClientByteChunk" should {
    "aggregate full content when size is not specified" in{
      val future = client(Chunk(Array[Byte]('1', '2'), Some(Future(Chunk(Array[Byte]('3', '4')))))).aggregate(None).get("foo")
      respondWithCode(future, OK)
      new String(future.futureValue.content.get.data) must equal ("1234")
    }

    "aggregate content up to the specified size" in{
      val future = client(Chunk(Array[Byte]('1', '2'), Some(Future(Chunk(Array[Byte]('3', '4')))))).aggregate(Some(2.bytes)).get("foo")
      respondWithCode(future, OK)
      new String(future.futureValue.content.get.data) must equal ("12")
    }
  }

  private def client(content: ByteChunk) = HttpClientImpl(content)

  case class HttpClientImpl(content: ByteChunk) extends HttpClientByteChunk{
    def isDefinedAt(request: HttpRequest[ByteChunk]) = true
    def apply(request: HttpRequest[ByteChunk]) = Future(HttpResponse[ByteChunk](content = Some(content)))
  }
}
