package blueeyes.core.service.engines

import blueeyes.core.http._
import blueeyes.core.http.test.HttpRequestCheckers
import blueeyes.core.data._
import blueeyes.core.service.HttpRequestHandlerCombinators
import blueeyes.core.http.HttpHeaders._
import blueeyes.core.http.MimeTypes._
import blueeyes.core.http.HttpStatusCodes._
import netty.NettyEngine
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll

import akka.dispatch.Future
import akka.dispatch.Promise
import akka.dispatch.Await
import akka.util.Duration
import blueeyes.bkka.AkkaDefaults

import collection.mutable.ArrayBuilder.ofByte
import blueeyes.json.JsonAST._
import blueeyes.json.Printer._

import org.streum.configrity.Configuration
import org.streum.configrity.io.BlockFormat

class HttpClientXLightWebSpec extends WordSpec with MustMatchers with BeforeAndAfterAll with HttpRequestCheckers with BijectionsChunkString with ContentReader with HttpRequestHandlerCombinators 
with AkkaDefaults {
  val duration = Duration(250, "milliseconds")
  val retries = 30

  private val httpClient = new HttpClientXLightWeb

  private val configPattern = """server {
    port = %d
    sslPort = %d
  }"""

  private var port = 8586
  private var server: Option[NettyEngine] = None
  private var uri = ""

  override def beforeAll() {
    var result: Option[EchoServer] = None 
    do {
      result = try {
        val config = Configuration.parse(configPattern.format(port, port + 1), BlockFormat)
        
        val echoServer = new EchoServer(config)

        echoServer.start
        Some(echoServer) 
      }
      catch {
        case e: Throwable => {
          e.printStackTrace()
          port = port + 2
          None 
        }
      }
    } while(result.isEmpty)

    server = result 

    uri = "http://localhost:%s/echo".format(port)
  }

  override def afterAll() {
    server.foreach{ _.stop }
  }


  "HttpClientXLightWeb" should {
    "Support GET to invalid server should return http error" in {
      val result = httpClient.get[String]("http://127.0.0.1:666/foo").failed
      result.futureValue.getClass must be (classOf[HttpException])
    }

    "Support GET to invalid URI/404 should cancel Future" in {
      httpClient.get(uri + "/bogus").failed.futureValue match {
        case HttpException(NotFound, _) => // Ok
        case other => fail("Expected HttpException(NotFound, _), but got: " + other)
      }
    }

    "Support GET requests with status OK" in {
      httpClient.parameters('param1 -> "a").get(uri).futureValue.content.get match {
        case _ : String => // Ok
        case other => fail("Expected _ : String, but got " + other)
      }
    }

    "Support GET requests with status Not Found" in {
      httpClient.get(uri + "/bogus").failed.futureValue match {
        case HttpException(NotFound, _) => // Ok
        case other => fail("Expected HttpException(NotFound, _), but got: " + other)
      }
    }

    "Support GET requests with query params" in {
      httpClient.get(uri + "?param1=a&param2=b").futureValue.content.get.trim must equal ("param1=a&param2=b".trim)
    }

    "Support GET requests with request params" in {
      httpClient.parameters('param1 -> "a", 'param2 -> "b").get(uri).futureValue.content.get.trim must equal ("param1=a&param2=b".trim) 
    }

    "Support POST requests with query params" in {
      httpClient.post(uri + "?param1=a&param2=b")("").futureValue.content.get.trim must equal ("param1=a&param2=b".trim)
    }

    "Support POST requests with request params" in {
      httpClient.parameters('param1 -> "a", 'param2 -> "b").post(uri)("").futureValue.content.get.trim must equal ("param1=a&param2=b".trim)
    }

    "Support POST requests with body" in {
      val expected = "Hello, world"
      httpClient.post(uri)(expected).futureValue.content.get.trim must equal (expected.trim)
    }

    "Support POST requests with body and request params" in {
      val expected = "Hello, world"
      httpClient.parameters('param1 -> "a", 'param2 -> "b").post(uri)(expected).futureValue.content.get.trim must equal (("param1=a&param2=b" + expected).trim)
    }

    "Support PUT requests with body" in {
      val expected = "Hello, world"
      httpClient.header(`Content-Length`(100)).put(uri)(expected).futureValue.content.get.trim must equal (expected.trim)
    }

    "Support GET requests with header" in {
      val content = httpClient.header("Fooblahblah" -> "washere").header("param2" -> "1").get(uri + "?headers=true").futureValue.content.get
      content.indexOf("Fooblahblah: washere") must be >= 0
      content.indexOf("param2: 1") must be >= 0
    }

    "Support POST requests with Content-Type: text/html & Content-Length: 100" in {
      val expected = "<html></html>"
      httpClient.headers(`Content-Type`(text/html) :: `Content-Length`(100) :: Nil).post(uri)(expected).futureValue match {
        case HttpResponse(status, _, Some(content), _) =>
          status.code must equal (HttpStatusCodes.OK)
          content.trim must equal (expected)
        case other => fail("Expected HttpResponse(status, _, Some(content), _), but got: " + other)
      }
    }

    "Support POST requests with large payload" in {
      val expected = Array.fill(2048*1000)(0).toList.mkString("")
      httpClient.post(uri)(expected).futureValue.content.get must equal (expected)      
    }
    "Support POST requests with large payload with several chunks" in {
      val expected = Array.fill[Byte](2048*100)('0')
      val chunk   = Chunk(expected, Some(Future(Chunk(expected))))
      httpClient.post[ByteChunk](uri)(chunk).futureValue match {
        case HttpResponse(status, _, Some(content), _) =>
          status.code must equal (HttpStatusCodes.OK)
          new String(content.data).length must equal (new String(expected ++ expected).length)
        case other => fail("Expected HttpResponse(status, _, Some(content), _), but got: " + other)
      }
    }

   "Support HEAD requests" in {
      httpClient.head(uri).futureValue match {
        case HttpResponse(status, _, _, _) =>
          status.code must equal (HttpStatusCodes.OK)
        case other => fail("Expected HttpResponse(status, _, _, _), but got: " + other)
      }
    }

   "Support response headers" in {
      httpClient.get(uri).futureValue match {
        case HttpResponse(status, headers, _, _) =>
          status.code must equal (HttpStatusCodes.OK)
          headers.raw must contain key ("kludgy")
      }
    }

    "Support GET requests of 1000 requests" in {
      val total = 1000
      val duration = Duration(1000, "milliseconds")
      val futures = List.fill(total) {
        httpClient.get(uri + "?test=true")
      }

      Future.sequence(futures).futureValue match {
        case responses =>
          responses.size must equal (total)
          responses.foreach {
            response => response.status.code must equal (HttpStatusCodes.OK)
          }
      }
    }

    "Support typed GET requests with query params" in {
      httpClient.get[String](uri + "?param1=a&param2=b").futureValue.content.get.trim must equal ("param1=a&param2=b")
    }

    "Support POST requests with body with Array[Byte]" in {
      import BijectionsChunkByteArray._
      import BijectionsByteArray._
      val expected = "Hello, world"
      ByteArrayToString(httpClient.post(uri)(StringToByteArray(expected)).futureValue.content.get) must equal (expected)
    }

    "Support POST requests with body several chunks and transcoding" in {
      import BijectionsChunkByteArray._
      import BijectionsByteArray._
      implicit val bijection = chunksToChunksArrayByte[String]

      val chunks: Chunk[String] = Chunk("foo", Some(Future[Chunk[String]](Chunk("bar"))))

      val response = Await.result(httpClient.post(uri)(chunks)(bijection), duration)
      response.status.code must be (HttpStatusCodes.OK)
      readContent(response.content.get).map(_.mkString("")).futureValue must equal ("foobar")
    }

    "Support POST requests with encoded URL should be preserved" in {
      val content = "Hello, world"
      httpClient.post(uri + "?headers=true&plckForumId=Cat:Wedding%20BoardsForum:238")(content).futureValue match {
        case HttpResponse(status, _, Some(content), _) =>
          status.code must equal (HttpStatusCodes.OK)
          content.indexOf("plckForumId=Cat:Wedding BoardsForum:238") must be >= 0
        case other => fail("Expected HttpResponse(status, _, Some(content), _), but got: " + other)
      }
    }
  }
}

import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.service._

class EchoServer(configOverride: Configuration) extends EchoService with HttpReflectiveServiceList[ByteChunk] with NettyEngine { 
  override def rootConfig = configOverride
}

trait ContentReader extends AkkaDefaults {
  def readContent[T](chunk: Chunk[T]): Future[List[T]] = {
    val promise = Promise[List[T]]()
    readContent[T](chunk, List[T](), promise)
    promise
  }
  def readContent[T](chunk: Chunk[T], chunks: List[T], promise: Promise[List[T]]) {
    val newChunks = chunks ::: List(chunk.data)

    chunk.next match{
      case Some(x) => x.foreach(nextChunk => readContent(nextChunk, newChunks, promise))
      case None => promise.success(newChunks)
    }
  }
}

trait EchoService extends BlueEyesServiceBuilder with BijectionsChunkString with ContentReader with AkkaDefaults {
  import blueeyes.core.http.MimeTypes._

  private implicit val ordering = new Ordering[Symbol] {
    def compare(l: Symbol, r: Symbol): Int = {
      l.name.compare(r.name)
    }
  }

  private def response(content: Option[String] = None) =
    HttpResponse[String](status = HttpStatus(HttpStatusCodes.OK), content = content, headers = Map("kludgy" -> "header test"))

  private def handler(request: HttpRequest[ByteChunk]): Future[HttpResponse[String]] = {
    val params = request.parameters.toList.sorted.foldLeft(List[String]()) { (l: List[String], p: Tuple2[Symbol, String]) =>
      l ++ List("%s=%s".format(p._1.name, p._2))
    }.mkString("&")

    val headers = request.parameters.get('headers).map { o =>
      request.headers.raw.toList.sorted.foldLeft(List[String]()) { (l, h) =>
    	l ++ List("%s: %s".format(h._1, h._2))
      }.mkString("&")
    }.getOrElse("")

    val content: Future[String] = request.content.map(readStringContent).getOrElse(Promise.successful(""))

    val promise = Promise[HttpResponse[String]]()
    content foreach   { v => promise.success(response(content = Some(params + v + headers))) }
    content onFailure { case th => promise.failure(th) }
    promise
  }

  private def readStringContent(chunk: ByteChunk) = {
    val result = readContent(chunk)
    result.map(_.map(v => new String(v, "UTF-8")).mkString(""))
  }

  private def readContent(chunk: ByteChunk, buffer: ofByte, promise: Promise[String]) {
    buffer ++= chunk.data

    chunk.next match{
      case Some(x) => x.foreach(nextChunk => readContent(nextChunk, buffer, promise))
      case None => promise.success(new String(buffer.result, "UTF-8"))
    }
  }

  val echoService: Service[ByteChunk] = service("echo", "1.0.0") { context =>
    request {
      path("/echo") {
        produce(text/html) {
          get(handler _) ~
	        post(handler _) ~
	        put(handler _) ~
	        head(handler _)
        }
      }
    }
  }
}
