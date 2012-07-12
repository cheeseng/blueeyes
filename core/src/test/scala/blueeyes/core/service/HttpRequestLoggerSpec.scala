package blueeyes.core.service

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import blueeyes.parsers.W3ExtendedLogAST._
import blueeyes.core.http._
import akka.dispatch.Future
import blueeyes.util.ClockMock
import org.joda.time.format.DateTimeFormat
import java.net.InetAddress
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.data.BijectionsChunkString._
import org.apache.commons.codec.binary.Base64

import blueeyes.bkka.AkkaDefaults
import blueeyes.concurrent.test.AkkaFutures

class HttpRequestLoggerSpec extends WordSpec with MustMatchers with ClockMock with AkkaFutures with AkkaDefaults {

  private val DateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
  private val TimeFormatter = DateTimeFormat.forPattern("HH:mm:ss.S")

  private val request        = HttpRequest[String](content = Some("request content"), method = HttpMethods.GET, uri = "/foo/bar?param=value", remoteHost = Some(InetAddress.getLocalHost), headers = Map[String, String]("content-language" -> "en"))
  private val response       = HttpResponse[String](content = Some("response content"), status = HttpStatus(Created), headers = Map[String, String]("content-length" -> "1000", "age" -> "3"))
  private val responseFuture = Future(response)

  "HttpRequestLogger: logs multiple values" in {
    log(DateIdentifier, TimeIdentifier).futureValue must be ((DateIdentifier, Left(DateFormatter.print(clockMock.now()))) :: (TimeIdentifier, Left(TimeFormatter.print(clockMock.now()))) :: Nil)
  }
  "HttpRequestLogger: logs date" in {
    log(DateIdentifier).futureValue must be ((DateIdentifier, Left(DateFormatter.print(clockMock.now()))) :: Nil)
  }
  "HttpRequestLogger: logs time" in {
    log(TimeIdentifier).futureValue must be ((TimeIdentifier, Left(TimeFormatter.print(clockMock.now()))) :: Nil) 
  }
  "HttpRequestLogger: logs time taken" in {
    log(TimeTakenIdentifier).futureValue must be ((TimeTakenIdentifier, Left("0.0")) :: Nil) 
  }
  "HttpRequestLogger: logs bytes" in {
    log(BytesIdentifier).futureValue must be ((BytesIdentifier, Left("1000")) :: Nil) 
  }
  "HttpRequestLogger: logs cached" in {
    log(CachedIdentifier).futureValue must be ((CachedIdentifier, Left("1")) :: Nil) 
  }
  "HttpRequestLogger: logs client ip" in {
    log(IpIdentifier(ClientPrefix)).map(Some(_)).futureValue must be (request.remoteHost.map(v => (IpIdentifier(ClientPrefix), Left(v.getHostAddress)) :: Nil))
  }
  "HttpRequestLogger: logs server ip" in {
    log(IpIdentifier(ServerPrefix)).futureValue must be ((IpIdentifier(ServerPrefix), Left(InetAddress.getLocalHost.getHostAddress)) :: Nil) 
  }
  "HttpRequestLogger: logs client dns" in {
    log(DnsNameIdentifier(ClientPrefix)).map(Some(_)).futureValue must be (request.remoteHost.map(v => (DnsNameIdentifier(ClientPrefix), Left(v.getHostName)) :: Nil))
  }
  "HttpRequestLogger: logs server dns" in {
    log(DnsNameIdentifier(ServerPrefix)).futureValue must be ((DnsNameIdentifier(ServerPrefix), Left(InetAddress.getLocalHost.getHostName)) :: Nil)
  }
  "HttpRequestLogger: logs Status" in {
    log(StatusIdentifier(ServerToClientPrefix)).futureValue must be ((StatusIdentifier(ServerToClientPrefix), Left(response.status.code.name)) :: Nil)
  }
  "HttpRequestLogger: logs comment" in {
    log(CommentIdentifier(ServerToClientPrefix)).futureValue must be ((CommentIdentifier(ServerToClientPrefix), Left(response.status.reason)) :: Nil)
  }
  "HttpRequestLogger: logs method" in {
    log(MethodIdentifier(ClientToServerPrefix)).futureValue must be ((MethodIdentifier(ClientToServerPrefix), Left(request.method.value)) :: Nil)
  }
  "HttpRequestLogger: logs uri" in {
    log(UriIdentifier(ClientToServerPrefix)).futureValue must be ((UriIdentifier(ClientToServerPrefix), Left(request.uri.toString)) :: Nil)
  }
  "HttpRequestLogger: logs uri-stem" in {
    log(UriStemIdentifier(ClientToServerPrefix)).map(Some(_)).futureValue must be (request.uri.path.map(v => (UriStemIdentifier(ClientToServerPrefix), Left(v)) :: Nil))
  }
  "HttpRequestLogger: logs uri-query" in {
    log(UriQueryIdentifier(ClientToServerPrefix)).map(Some(_)).futureValue must be (request.uri.query.map(v => (UriQueryIdentifier(ClientToServerPrefix), Left(v)) :: Nil))
  }
  "HttpRequestLogger: logs request header" in {
    log(HeaderIdentifier(ClientToServerPrefix, "content-language")).futureValue must be ((HeaderIdentifier(ClientToServerPrefix, "content-language"), Left("en")) :: Nil)
  }
  "HttpRequestLogger: logs response header" in {
    log(HeaderIdentifier(ServerToClientPrefix, "age")).futureValue must be ((HeaderIdentifier(ServerToClientPrefix, "age"), Left("3.0")) :: Nil)
  }
  "HttpRequestLogger: logs request content" in {
    log(ContentIdentifier(ClientToServerPrefix)).map(_.map(toStringValues)).futureValue must be (List((ContentIdentifier(ClientToServerPrefix), Right(request.content.get))))
  }
  "HttpRequestLogger: logs response content" in {
    log(ContentIdentifier(ServerToClientPrefix)).map(_.map(toStringValues)).futureValue must be (List((ContentIdentifier(ServerToClientPrefix), Right(response.content.get))))
  }

  private def toStringValues(v: (FieldIdentifier, Either[String, Array[Byte]])): Tuple2[FieldIdentifier, Either[String, String]] = {
    val value = v._2 match{
      case Right(value) => Right[String, String](new String(value, "UTF-8"))
      case Left(value) => Left[String, String](value)
    }
    Tuple2[FieldIdentifier, Either[String, String]](v._1, value)
  }

  private def log(fieldIdentifiers: FieldIdentifier*): Future[List[(FieldIdentifier, Either[String, Array[Byte]])]] = HttpRequestLogger[String, String](FieldsDirective(List(fieldIdentifiers: _*))).apply(request, responseFuture)
}
