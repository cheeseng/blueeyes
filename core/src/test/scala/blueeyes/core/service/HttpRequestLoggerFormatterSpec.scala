package blueeyes.core.service

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import blueeyes.parsers.W3ExtendedLogAST.{FieldIdentifier, FieldsDirective}

class HttpRequestLoggerFormatterSpec extends WordSpec with MustMatchers {
  "HttpRequestLoggerFormatter" should{
    "create w3c formatter" in{
      HttpRequestLoggerFormatter("w3c").isInstanceOf[HttpRequestLoggerW3CFormatter] must equal (true)
    }
    "create json formatter" in{
      HttpRequestLoggerFormatter("json").isInstanceOf[HttpRequestLoggerJsonFormatter] must equal (true)
    }
    "create custom formatter" in{
      HttpRequestLoggerFormatter("blueeyes.core.service.HttpRequestLoggerFormatterImpl").isInstanceOf[HttpRequestLoggerFormatterImpl] must equal (true)
    }
  }
}

class HttpRequestLoggerFormatterImpl extends HttpRequestLoggerFormatter{
  def formatLog(log: List[(FieldIdentifier, Either[String, Array[Byte]])]) = ""

  def formatHeader(fieldsDirective: FieldsDirective) = ""
}
