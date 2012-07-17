package blueeyes
package core.service.engines
package netty

import blueeyes.core.service._
import collection.mutable.ArrayBuilder.ofByte
import engines.security.BlueEyesKeyStoreFactory
import engines.{TestEngineService, TestEngineServiceContext, HttpClientXLightWeb}
import org.scalatest._

import akka.dispatch.Future
import akka.dispatch.Promise
import akka.dispatch.Await
import akka.util._

import blueeyes.core.http.MimeTypes._
import blueeyes.core.http._
import blueeyes.core.data.{FileSink, FileSource, Chunk, ByteChunk, BijectionsByteArray, BijectionsChunkString}
import blueeyes.core.http.combinators.HttpRequestCombinators
import blueeyes.core.http.HttpStatusCodes._

import java.io.File
import java.util.concurrent.CountDownLatch
import javax.net.ssl.TrustManagerFactory

import org.streum.configrity.Configuration
import org.streum.configrity.io.BlockFormat

import blueeyes.concurrent.test.AkkaFutures

class HttpServerNettySpec extends WordSpec with MustMatchers with BeforeAndAfterAll with BijectionsByteArray with BijectionsChunkString with blueeyes.bkka.AkkaDefaults with AkkaFutures {

  private val configPattern = """server {
  port = %d
  sslPort = %d
}"""

  val duration = Duration(50000, "milliseconds")
  val retries = 50

  private var port = 8585
  private var server: Option[NettyEngine] = None

  override def beforeAll(configMap: Map[String, Any]) {
    var error: Option[Throwable] = None
    do{
      val config = Configuration.parse(configPattern.format(port, port + 1), BlockFormat)
      val sampleServer = new SampleServer(config)
      val doneSignal   = new CountDownLatch(1)

      val startFuture = sampleServer.start

      startFuture.onSuccess { case _ =>
        error = None
        doneSignal.countDown()
      }
      startFuture.onFailure { case v =>
        println("Error trying to start server on ports " + port + ", " + (port + 1))
        error = Some(v)
        port  = port + 2
        doneSignal.countDown()
      }

      server = Some(sampleServer)

      doneSignal.await()
    }while(error != None)
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
      val response = Await.result(client.post("/foo/foo/adCode.html")("foo").failed, duration)
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
      client.parameters('bar -> "zar").get("/foo").futureValue match {
        case HttpResponse(status, _, content, _) =>
          status.code must be (OK)
          content must be (Some(TestEngineServiceContext.context))
        case other => fail("Expected HttpResponse(status, _, content, _), but got: " + other)
      }
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
        case other => fail("Expected HttpResponse(status, _, content, _), but got: " + other)
      }
    }
    "return html by correct URI by https" in{
      sslClient.get("/bar/foo/adCode.html").futureValue match {
        case HttpResponse(status, _, content, _) =>
          content.get must equal (TestEngineServiceContext.context)
        case other => fail("Expected HttpResponse(status, _, content, _), but got: " + other)
      }
    }
    "return huge content by https"in{
      sslClient.get[ByteChunk]("/huge").futureValue match {
        case HttpResponse(status, _, content, _) =>
          status.code must be (OK)
          content.map(v => readContent(v)) must be (Some(TestEngineServiceContext.hugeContext.map(v => new String(v).mkString("")).mkString("")))
        case other => fail("Expected HttpResponse(status, _, content, _), but got: " + other)
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
  private def client    = new LocalHttpsClient(server.get.config).protocol("http").host("localhost").port(port)
  private def sslClient = new LocalHttpsClient(server.get.config).protocol("https").host("localhost").port(port + 1)
}

class SampleServer(configOverride: Configuration) extends TestEngineService with HttpReflectiveServiceList[ByteChunk] with NettyEngine {
  override def rootConfig = configOverride
} 

class LocalHttpsClient(config: Configuration) extends HttpClientXLightWeb {
  override protected def createSSLContext = {
    val keyStore            = BlueEyesKeyStoreFactory(config)
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(keyStore)

    SslContextFactory(keyStore, BlueEyesKeyStoreFactory.password, Some(trustManagerFactory.getTrustManagers))
  }
}

trait SampleService extends BlueEyesServiceBuilder with HttpRequestCombinators with BijectionsChunkString{
  import blueeyes.core.http.MimeTypes._

  private val response = HttpResponse[String](status = HttpStatus(HttpStatusCodes.OK), content = Some(Context.context))

  val sampleService: Service[ByteChunk] = service("sample", "1.32") { context =>
    request {
      produce(text/html) {
        path("/bar/'adId/adCode.html") {
          get { request: HttpRequest[ByteChunk] =>
            Future[HttpResponse[String]](response)
          }
        } ~
        path("/foo") {
          get { request: HttpRequest[ByteChunk] =>
            Future[HttpResponse[String]](response)
          }
        } ~
        path("/error") {
          get[ByteChunk, Future[HttpResponse[String]]] { request: HttpRequest[ByteChunk] =>
            throw new RuntimeException("Unexpected error (GET /error)")
          }
        } ~
        path("/http/error") {
          get[ByteChunk, Future[HttpResponse[String]]] { request: HttpRequest[ByteChunk] =>
            throw HttpException(HttpStatusCodes.BadRequest)
          }
        }
      } ~
      path("/huge"){
        get { request: HttpRequest[ByteChunk] =>
          val chunk  = Chunk(Context.hugeContext.head, Some(Future(Chunk(Context.hugeContext.tail.head))))

          val response     = HttpResponse[ByteChunk](status = HttpStatus(HttpStatusCodes.OK), content = Some(chunk))
          Future[HttpResponse[ByteChunk]](response)
        }
      } ~
      path("/empty/response"){
        post { request: HttpRequest[ByteChunk] =>
          Future[HttpResponse[ByteChunk]](HttpResponse[ByteChunk]())
        }
      } ~
      path("/file/write"){
        post { request: HttpRequest[ByteChunk] =>
          val promise = Promise[HttpResponse[ByteChunk]]()
          for (value <- request.content) {
            FileSink.write(Context.dataFile, value).onSuccess { 
              case _ => promise.success(HttpResponse[ByteChunk]()) 
            } 
          }
          promise
        }
      } ~
      path("/file/read"){
        get { request: HttpRequest[ByteChunk] =>
          val response     = HttpResponse[ByteChunk](status = HttpStatus(HttpStatusCodes.OK), content = FileSource(Context.dataFile))
          Future[HttpResponse[ByteChunk]](response)
        }
      } ~
      path("/huge/delayed"){
        get { request: HttpRequest[ByteChunk] =>

          val promise = Promise[ByteChunk]()
          import scala.actors.Actor.actor
          actor {
            Thread.sleep(2000)
            promise.success(Chunk(Context.hugeContext.tail.head))
          }

          val chunk  = Chunk(Context.hugeContext.head, Some(promise))

          val response     = HttpResponse[ByteChunk](status = HttpStatus(HttpStatusCodes.OK), content = Some(chunk))
          Future[HttpResponse[ByteChunk]](response)
        }
      }
    }
  }
}

object Context{
  val dataFile = new File(System.getProperty("java.io.tmpdir") + File.separator + System.currentTimeMillis)

  val hugeContext = List[Array[Byte]]("first-".getBytes ++ Array.fill[Byte](2048*1000)('0'), "second-".getBytes ++ Array.fill[Byte](2048*1000)('0'))
  val context = """<html>
<head>
</head>

<body>
    <h1>Test</h1>
    <h1>Test</h1>
</body>
</html>"""
}
