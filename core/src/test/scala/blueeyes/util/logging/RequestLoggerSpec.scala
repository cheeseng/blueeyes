package blueeyes.util.logging

import org.scalatest._
import blueeyes.parsers.W3ExtendedLogAST._
import RollPolicies._
import java.io.File
import scala.io.Source
import blueeyes.core.service.HttpRequestLoggerW3CFormatter
import akka.util.Timeout
import org.scalatest.concurrent.Eventually
import org.scalatest.time._

class RequestLoggerSpec extends WordSpec with MustMatchers with Eventually with BeforeAndAfter {
  private val directives = FieldsDirective(List(DateIdentifier, TimeIdentifier))
  private val formatter  = new HttpRequestLoggerW3CFormatter()

  override implicit val patienceConfig =
    PatienceConfig(timeout = scaled(Span(3, Seconds)), interval = scaled(Span(100, Millis)))

  private var w3Logger: RequestLogger = _
  "W3ExtendedLogger" should {
    def header() = formatter.formatHeader(directives)

    "creates log file" in {
      w3Logger = RequestLogger.get(System.getProperty("java.io.tmpdir") + File.separator + "w3.log", Never, header _, 1)

      new File(w3Logger.fileName.get).exists must equal (true)
    }
    "init log file" in {
      w3Logger = RequestLogger.get(System.getProperty("java.io.tmpdir") + File.separator + "w3_1.log", Never, header _, 1)

      val content = getContents(new File(w3Logger.fileName.get))

      content.indexOf("#Version: 1.0")      must not equal (-1)
      content.indexOf("#Date: ")            must not equal (-1)
      content.indexOf(directives.toString)  must not equal (-1)
    }

    "flush entries while closing" in{
      w3Logger = RequestLogger.get(System.getProperty("java.io.tmpdir") + File.separator + "w3_2.log", Never, header _, 1)

      w3Logger("foo")
      w3Logger("bar")

      val future = w3Logger.close(akka.util.Timeout(5000))
      eventually { future.value.isDefined must be (true) }

      val content = getContents(new File(w3Logger.fileName.get))

      content.indexOf("foo") must not equal (-1)
      content.indexOf("bar") must not equal (-1)
    }

    "write log entries" in {
      w3Logger = RequestLogger.get(System.getProperty("java.io.tmpdir") + File.separator + "w3_3.log", Never, header _, 1)

      w3Logger("foo")
      w3Logger("bar")
      w3Logger("baz")

      val file = new File(w3Logger.fileName.get)

      eventually { getContents(file).indexOf("foo") must not equal (-1) }
      eventually { getContents(file).indexOf("bar") must not equal (-1) }
    }
  }

  def cleanUp(){
    val future = w3Logger.close(akka.util.Timeout(5000))
    eventually { future.value.isDefined must be (true) }

    new File(w3Logger.fileName.get).delete
  }

  after { cleanUp() }

  private def getContents(file: File) = Source.fromFile(file, "UTF-8").getLines.mkString("\n")
}
