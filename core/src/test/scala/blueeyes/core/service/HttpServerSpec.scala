package blueeyes.core.service

import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.http.test.HttpRequestCheckers
import akka.dispatch.{Await, Future}
import akka.util.Duration
import java.util.concurrent.TimeUnit._
import blueeyes.core.http.combinators.HttpRequestCombinators
import blueeyes.core.http.MimeTypes._
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.data.{ByteChunk, BijectionsChunkString}
import blueeyes.core.http._

import org.streum.configrity.Configuration
import org.streum.configrity.io.BlockFormat

import org.scalatest.fixture
import org.scalatest.matchers.MustMatchers
import org.scalatest.concurrent.Eventually

class HttpServerSpec extends fixture.WordSpec with MustMatchers with BijectionsChunkString with HttpRequestCheckers with Eventually {
  val timeout = Duration(10, SECONDS)
  type FixtureParam = TestServer

  def withFixture(test: OneArgTest) {
    val config = Configuration.parse("", BlockFormat)
    val server = new TestServer(config) ->- { s => Await.result(s.start, timeout) }
    test(server)
  }

  "HttpServer.start" should { 
    "executes start up function" in { server=>
      server.startupCalled must be (true)
    }
    "set status to Starting" in { server=>
      server.status must be (RunningStatus.Started)
    }
  }
  
  "HttpServer.apply" should {
    "delegate to service request handler" in { server=>
      server.service(HttpRequest[ByteChunk](HttpMethods.GET, "/foo/bar")).toOption.get.futureValue match {
        case HttpResponse(HttpStatus(status, _), headers, Some(content), _) =>
          status must equal (OK)
          ChunkToString(content) must equal ("blahblah")
          headers.get("Content-Type") must be (Some("text/plain"))
        case other => fail("Expected HttpResponse, but got: " +  other)
      }
    }
    
    "produce NotFound response when service is not defined for request" in { server=>
      respondWithCode(server.service(HttpRequest[ByteChunk](HttpMethods.GET, "/blahblah")).toOption.get, HttpStatusCodes.NotFound)
    }

    "gracefully handle error-producing service handler" in { server=>
      respondWithCode(server.service(HttpRequest[ByteChunk](HttpMethods.GET, "/foo/bar/error")).toOption.get, HttpStatusCodes.InternalServerError)
    }
    "gracefully handle dead-future-producing service handler" in { server=>
      respondWithCode(server.service(HttpRequest[ByteChunk](HttpMethods.GET, "/foo/bar/dead")).toOption.get, HttpStatusCodes.InternalServerError)
    }
  }

  "HttpServer stop" should {
    "execute shut down function" in { server=>
      val f = server.stop
      Await.result(f, timeout)
      server.shutdownCalled must be (true)
      eventually { server.status must be (RunningStatus.Stopped) }
    }
  }  
}

class TestServer(configOverride: Configuration) extends TestService with HttpReflectiveServiceList[ByteChunk] {
  override def rootConfig = configOverride
}

trait TestService extends HttpServer with BlueEyesServiceBuilder with HttpRequestCombinators with BijectionsChunkString with blueeyes.bkka.AkkaDefaults {
  var startupCalled   = false
  var shutdownCalled  = false
  lazy val testService = service("test", "1.0.7") {
    context => {
      startup {
        startupCalled = true
        Future("blahblah")
      } ->
      request { value: String =>
        path("/foo/bar") {
          produce(text/plain) {
            get {
              request: HttpRequest[ByteChunk] => Future(HttpResponse[String](content=Some(value)))
            } ~
            path("/error") { 
              get[ByteChunk, Future[HttpResponse[String]]] { request: HttpRequest[ByteChunk] =>
                sys.error("He's dead, Jim.")
              }
            } ~
            path("/dead") {
              get { request: HttpRequest[ByteChunk] =>
                akka.dispatch.Promise.failed[HttpResponse[String]](new RuntimeException())
              }
            }
          }
        }
      } ->
      shutdown { value =>
        Future(shutdownCalled = true)
      }
    }
  }
}
