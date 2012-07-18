package blueeyes.core.service.engines.servlet

import blueeyes.core.service._
import engines.{HttpClientXLightWeb, TestEngineService, TestEngineServiceContext}
import org.scalatest._
import collection.mutable.ArrayBuilder.ofByte

import blueeyes.core.http.MimeTypes._
import blueeyes.core.http._
import blueeyes.core.data.{ByteChunk, BijectionsByteArray, BijectionsChunkString}
import blueeyes.core.http.HttpStatusCodes._

import org.scalatest.BeforeAndAfterAll
import blueeyes.concurrent.test.AkkaFutures
import akka.dispatch.{Await, Promise}
import akka.util.Duration
import java.util.concurrent.TimeUnit

class HttpServerServletSpec extends WordSpec with MustMatchers with BeforeAndAfterAll with BijectionsByteArray with BijectionsChunkString with blueeyes.bkka.AkkaDefaults with AkkaFutures {
  val duration = Duration(10000, "millis")

  private var port= 8585
  private var server: Option[JettyServer] = None

  override def beforeAll(configMap: Map[String, Any]) {
    while(!server.isDefined){
      try{
        val server = new JettyServer(new ServletTestEngineService())
        server.start(port)

        this.server = Some(server)
      }
      catch {
        case e: Throwable => {
          e.printStackTrace()
          port = port + 1;
        }
      }
    }
  }

  override def afterAll(configMap: Map[String, Any]) {
    TestEngineServiceContext.dataFile.delete
    server.foreach(_.stop)
  }

  "HttpServer" should {
    "return empty response"in{
      client.post("/empty/response")("").futureValue match {
        case HttpResponse(status, _, content, _) =>
          status.code must be (OK)
          content must be (None)
        case other => fail("Expected HttpResponse(status, _, content, _), but got: " + other)
      }
    }

    "write file"in{
      TestEngineServiceContext.dataFile.delete
      client.post("/file/write")("foo").futureValue match {
        case HttpResponse(status, _, content, _) =>
          status.code must be (OK)
          TestEngineServiceContext.dataFile.exists must equal (true)
          TestEngineServiceContext.dataFile.length must equal ("foo".length)
        case other => fail("Expected HttpResponse(status, _, content, _), but got: " + other)
      }
    }

    "read file"in{
      TestEngineServiceContext.dataFile.delete

      akka.dispatch.Await.result(client.post("/file/write")("foo"), duration)
      client.get("/file/read").futureValue match {
        case HttpResponse(status, _, content, _) =>
          status.code must be (OK)
          content must be (Some("foo"))
        case other => fail("Expected HttpResponse(status, _, content, _), but got: " + other)
      }
    }

    "return html by correct URI" in{
      client.get("/bar/foo/adCode.html").futureValue match {
        case HttpResponse(status, _, content, _) =>
          status.code must be (OK)
          content must be (Some(TestEngineServiceContext.context))
        case other => fail("Expected HttpResponse(status, _, content, _), but got: " + other)
      }
    }

    "return NotFound when accessing a nonexistent URI" in{
      val response = Await.result(client.get("/foo/foo/adCode.html").failed, duration)
      response match {
        case HttpException(failure, _) => failure must equal (NotFound)
        case other => fail("Expected HttpException(failure, _), but got: " + other)
      }
    }
    "return InternalServerError when handling request crashes" in{
      val response = Await.result(client.get("/error").failed, duration)
      response match {
        case HttpException(failure, _) => failure must equal (InternalServerError)
        case other => fail("Expected HttpException(failure, _), but got: " + other)
      }
    }
    "return Http error when handling request throws HttpException" in {
      val response = Await.result(client.get("/http/error").failed, duration)
      response match {
        case HttpException(failure, _) => failure must equal (BadRequest)
        case other => fail("Expected HttpException(failure, _), but got: " + other)
      }
    }

    "return html by correct URI with parameters" in{
      client.parameters('bar -> "zar").get("/foo")
    }
    "return huge content"in{
      client.get[ByteChunk]("/huge").futureValue match {
        case HttpResponse(status, _, content, _) =>
          status.code must be (OK)
          content.map(v => readContent(v)) must be (Some(TestEngineServiceContext.hugeContext.map(v => new String(v).mkString("")).mkString("")))
        case other => fail("Expected HttpResponse(status, _, content, _), but got: " + other)
      }
    }
    "return huge delayed content"in{
      val content = client.get[ByteChunk]("/huge/delayed")
      content.futureValue match {
        case HttpResponse(status, _, content, _) =>
          status.code must be (OK)
          content.map(v => readContent(v)) must be (Some(TestEngineServiceContext.hugeContext.map(v => new String(v).mkString("")).mkString("")))
        case other => 
          fail("Expected HttpResponse(status, _, content, _), but got: " + other)
      }
    }
  }

  private def readContent(chunk: ByteChunk): String = {
    val promise = Promise[String]()
    readContent(chunk, new ofByte(), promise)
    akka.dispatch.Await.result(promise, duration)
  }

  private def readContent(chunk: ByteChunk, buffer: ofByte, promise: Promise[String]) {
    buffer ++= chunk.data

    chunk.next match{
      case Some(x) => x.onSuccess { case nextChunk => readContent(nextChunk, buffer, promise) }
      case None => promise.success(new String(buffer.result, "UTF-8"))
    }
  }

  private def client    = new HttpClientXLightWeb().protocol("http").host("localhost").port(port)

}

class ServletTestEngineService extends ServletEngine with TestEngineService with HttpReflectiveServiceList[ByteChunk]{ }
