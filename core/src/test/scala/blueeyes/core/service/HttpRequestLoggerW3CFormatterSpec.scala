package blueeyes.core.service

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.apache.commons.codec.binary.Base64
import blueeyes.parsers.W3ExtendedLogAST._

class HttpRequestLoggerW3CFormatterSpec extends WordSpec with MustMatchers {
  private val formatter = new HttpRequestLoggerW3CFormatter()
  "HttpRequestLoggerW3CFormatter" should{
    "format values in one line" in{
      formatter.formatLog((MethodIdentifier(ClientToServerPrefix), Left("GET")) :: (UriIdentifier(ClientToServerPrefix), Left("/foo/bar")) :: Nil) must equal ("GET /foo/bar")
    }
    "format content using Base64 encoding" in{
      val encoded = formatter.formatLog(Tuple2[FieldIdentifier, Either[String, Array[Byte]]](ContentIdentifier(ClientToServerPrefix), Right("content".getBytes("UTF-8"))) :: Nil)
      decodeBase64(encoded) must equal ("content")
    }
  }

  private def decodeBase64(data: String) = new String(Base64.decodeBase64(data.substring(1, data.length - 1)), "UTF-8")

}
