package blueeyes.core.data

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfter
import java.io.File
import akka.dispatch.Future
import akka.dispatch.Promise
import akka.dispatch.Await
import akka.util.Duration
import blueeyes.bkka.AkkaDefaults
import blueeyes.concurrent.test.AkkaFutures
import collection.mutable.ArrayBuilder.ofByte
import akka.util.duration._

trait Data extends AkkaDefaults {
  val dataFile = new File(System.getProperty("java.io.tmpdir") + File.separator + System.currentTimeMillis)
  val data     = List.fill(5)(List.fill[Byte](10)('0'))
  val chunk    = data.tail.foldLeft(Chunk(data.head.toArray)) { (chunk, data) => 
                   Chunk(data.toArray, Some(Future[ByteChunk](chunk)))
                 }
}

class FileSinkSpec extends WordSpec with MustMatchers with Data with BeforeAndAfter with AkkaDefaults with AkkaFutures {

  "FileSink" should {
    "write data" in{
      FileSink.write(dataFile, chunk).futureValue
      dataFile.exists must equal (true) 
      dataFile.length must equal (data.flatten.length)
    }

    "cancel result when write failed" in{
      val error  = new RuntimeException
      val result = FileSink.write(dataFile, Chunk(data.head.toArray, Some(Future(throw error))))

      val err = intercept[RuntimeException] { result.futureValue }
      err.getCause must be theSameInstanceAs error
      dataFile.exists must equal (true)
      dataFile.length must equal (data.head.toArray.length)
    }

    "cancel result when write getting next chunk failed" in{
      val error  = new RuntimeException
      val result = FileSink.write(dataFile, Chunk(data.head.toArray, Some(Promise.failed[ByteChunk](error))))

      val err = intercept[RuntimeException] { result.futureValue }
      err.getCause must be theSameInstanceAs error
      dataFile.exists must equal (true)
      dataFile.length must equal (data.head.toArray.length)
    }

    "cancel writing when result is canceled" in{
      val promise = Promise[ByteChunk]()
      val result = FileSink.write(dataFile, Chunk(data.head.toArray, Some(promise)))

      val killed = new RuntimeException("killed")
      result.asInstanceOf[Promise[Unit]].failure(killed)
      
      defaultActorSystem.scheduler.scheduleOnce(2000 millis) {
        promise.success(Chunk(data.head.toArray))
      }

      val err = intercept[RuntimeException] { promise.futureValue }
      err.getCause must be theSameInstanceAs killed
      dataFile.exists must equal (true)
      dataFile.length must equal (data.head.toArray.length)
    }
  }

  before { dataFile.delete }
  after { dataFile.delete }
}

class FileSourceSpec extends WordSpec with MustMatchers with Data with BeforeAndAfter with AkkaDefaults with AkkaFutures {

  "FileSource" should {
    "read all data" in{
      def readContent(chunk: ByteChunk, buffer: ofByte): ofByte = {
        buffer ++= chunk.data

        val next = chunk.next
        next match{
          case None =>  buffer
          case Some(x) => readContent(Await.result(x, Duration.Inf), buffer)
        }
      }

      val result = FileSink.write(dataFile, chunk)
      result.futureValue must be (())

      val fileChunks = FileSource(dataFile)

      new String(readContent(chunk, new ofByte()).result) must equal (new String(data.flatten.toArray))
    }
  }

  after { dataFile.delete }
}
