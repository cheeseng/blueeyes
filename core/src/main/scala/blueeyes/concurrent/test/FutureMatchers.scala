import scala.annotation.tailrec  
import org.specs2.execute.FailureException
import java.util.concurrent.{TimeoutException,  CountDownLatch}

import akka.dispatch.Future
import akka.dispatch.Await
import akka.util.Duration
import akka.util.DurationLong
import akka.util.duration._

import blueeyes.util.RichThrowableImplicits._

import org.specs2.matcher._

package blueeyes.concurrent.test {

trait AkkaConversions {
  implicit def specsDuration2Akka(duration: org.specs2.time.Duration): akka.util.Duration = new DurationLong(duration.inMillis).millis
  implicit def specsDuration2Rich(duration: org.specs2.time.Duration) = new RichSpecsDuration(duration)

  class RichSpecsDuration(duration: org.specs2.time.Duration) {
    def toAkka = specsDuration2Akka(duration)
  }
}

trait FutureMatchers extends AkkaConversions { 
  case class FutureTimeouts(retries: Int, duration: Duration)

  private sealed trait Outcome[A]
  private case class Done[A](matchResult: MatchResult[A]) extends Outcome[A]
  private case class Retry[A](failureMessage: String) extends Outcome[A]

  implicit val defaultFutureTimeouts: FutureTimeouts = FutureTimeouts(10, 100L millis)

  case class whenDelivered[A](matcher: Matcher[A])(implicit timeouts: FutureTimeouts) extends Matcher[Future[A]] with Expectations {
    def apply[B <: Future[A]](expectable: Expectable[B]): MatchResult[B] = {
      val (ok, okMessage, koMessage) = retry(expectable.evaluate.value, timeouts.retries, timeouts.retries)
      result(ok, okMessage, koMessage, expectable)
    }

    @tailrec
    private def retry[B <: Future[A]](future: => B, retries: Int, totalRetries: Int): (Boolean, String, String) = {
      val start = System.currentTimeMillis
      
      val outcome: Outcome[A] = try {
        val result = Await.result(future, timeouts.duration)
        val protoResult = matcher(result aka "The value returned from the Future")

        if (protoResult.isSuccess || retries <= 0) Done(protoResult)
        else protoResult match{
          case f @ MatchFailure(ok, ko, _, _) => Retry(ko)
          case f @ MatchSkip(m, _)            => Retry(m)
          case _ => Retry(protoResult.message)
        }
      } catch {
        case timeout: TimeoutException => Retry("Retried " + (totalRetries - retries) + " times with interval of " + timeouts.duration + " but did not observe a result.")
        case failure: FailureException => Retry("Assertion failed on retry " + (totalRetries - retries) + ": " + failure.f.message)
        case ex: Throwable             => Retry("Delivery of future was canceled on retry " + (timeouts.retries - retries) + ": " + ex.fullStackTrace)
      }  

      outcome match {
        case Done(result) => (result.isSuccess, result.message, result.message)

        case Retry(_) if (retries > 0) => 
          val end = System.currentTimeMillis
          Thread.sleep(0L.max(timeouts.duration.toMillis - (end - start)))
          print(".")
          retry(future, retries - 1, totalRetries)

        case Retry(message) => (false, "This message should never be seen.", message)
      }
    }
  }
}

import org.scalatest.concurrent.Futures
import org.scalatest.exceptions._
import org.scalatest.time._
import java.lang.annotation._
import java.nio.charset.CoderMalfunctionError
import javax.xml.parsers.FactoryConfigurationError
import javax.xml.transform.TransformerFactoryConfigurationError
import java.util.concurrent.TimeUnit

trait AkkaFutures extends Futures {

  implicit val defaultPatience =
    PatienceConfig(timeout =  Span(1000, Millis), interval = Span(100, Millis))

  implicit def convertAkkaFuture[T](akkaFuture: Future[T]): FutureConcept[T] = 
    new FutureConcept[T] {    
      def isExpired = false
      def isCanceled = false
      def eitherValue = akkaFuture.value
      
      override def futureValue(implicit config: PatienceConfig): T = {
        
        val st = Thread.currentThread.getStackTrace
        val callerStackFrame =
          if (!st(2).getMethodName.contains("futureValue"))
            st(2)
          else
            st(3)

        val methodName =
          if (callerStackFrame.getFileName == "Futures.scala" && callerStackFrame.getMethodName == "whenReady")
            "whenReady"
          else if (callerStackFrame.getFileName == "Futures.scala" && callerStackFrame.getMethodName == "isReadyWithin")
            "isReadyWithin"
          else
            "futureValue"

        val adjustment =
          if (methodName == "whenReady")
            3
          else
            0
            
        def wrapActorFailure(e: Throwable) = {
          val cause = e.getCause
            val exToReport = if (cause == null) e else cause // TODO: in 2.0 add TestCanceledException here
            if (anErrorThatShouldCauseAnAbort(exToReport) || exToReport.isInstanceOf[TestPendingException] || exToReport.isInstanceOf[TestCanceledException]) 
              exToReport
            else
              new TestFailedException(
                sde => Some {
                  if (exToReport.getMessage == null)
                    "The future passed to whenReady returned an exception of type: " + exToReport.getClass.getName + "."
                  else
                    "The future passed to whenReady returned an exception of type: " + exToReport.getClass.getName + ", with message: " + exToReport.getMessage + "."
                },
                Some(exToReport),
                getStackDepthFun("AkkaFutures.scala", methodName, adjustment)
              ) 
        }
            
        akkaFuture onFailure {
          case e: Throwable => throw wrapActorFailure(e)
        }
            
        try {
          Await.result(akkaFuture, Duration(config.timeout.totalNanos, TimeUnit.NANOSECONDS))
        }
        catch {
          case e: java.util.concurrent.TimeoutException =>
            throw new TestFailedException(
              sde => Some("The future passed to whenReady was never ready, so whenReady timed out. Waited " +config.timeout.totalNanos + " nanoseconds."),
              None,
              getStackDepthFun("AkkaFutures.scala", methodName, adjustment)
            ) with TimeoutField {
              val timeout: Span = config.timeout
            }
          case e: Throwable =>
            // Should not reach here (as it should be handled by onFailure above), but added just in case
            throw wrapActorFailure(e)
        }
      }
    }

  private def getStackDepthFun(fileName: String, methodName: String, adjustment: Int = 0): (StackDepthException => Int) = { sde =>
    getStackDepth(sde.getStackTrace, fileName, methodName, adjustment)
  }

  private def getStackDepth(stackTrace: Array[StackTraceElement], fileName: String, methodName: String, adjustment: Int = 0) = {
    val stackTraceList = stackTrace.toList

    val fileNameIsDesiredList: List[Boolean] =
      for (element <- stackTraceList) yield
        element.getFileName == fileName // such as "Checkers.scala"

    val methodNameIsDesiredList: List[Boolean] =
      for (element <- stackTraceList) yield
        element.getMethodName == methodName // such as "check"

    // For element 0, the previous file name was not desired, because there is no previous
    // one, so you start with false. For element 1, it depends on whether element 0 of the stack trace
    // had the desired file name, and so forth.
    val previousFileNameIsDesiredList: List[Boolean] = false :: (fileNameIsDesiredList.dropRight(1))

    // Zip these two related lists together. They now have two boolean values together, when both
    // are true, that's a stack trace element that should be included in the stack depth.
    val zipped1 = methodNameIsDesiredList zip previousFileNameIsDesiredList
    val methodNameAndPreviousFileNameAreDesiredList: List[Boolean] =
      for ((methodNameIsDesired, previousFileNameIsDesired) <- zipped1) yield
        methodNameIsDesired && previousFileNameIsDesired

    // Zip the two lists together, that when one or the other is true is an include.
    val zipped2 = fileNameIsDesiredList zip methodNameAndPreviousFileNameAreDesiredList
    val includeInStackDepthList: List[Boolean] =
      for ((fileNameIsDesired, methodNameAndPreviousFileNameAreDesired) <- zipped2) yield
        fileNameIsDesired || methodNameAndPreviousFileNameAreDesired

    val includeDepth = includeInStackDepthList.takeWhile(include => include).length
    val depth = if (includeDepth == 0 && stackTrace(0).getFileName != fileName && stackTrace(0).getMethodName != methodName) 
      stackTraceList.takeWhile(st => st.getFileName != fileName || st.getMethodName != methodName).length
    else
      includeDepth
    
    depth + adjustment
  }

  private def anErrorThatShouldCauseAnAbort(throwable: Throwable) =
    throwable match {
      case _: AnnotationFormatError | 
           _: CoderMalfunctionError |
           _: FactoryConfigurationError | 
           _: LinkageError | 
           _: ThreadDeath | 
           _: TransformerFactoryConfigurationError | 
           _: VirtualMachineError => true
      // Don't use AWTError directly because it doesn't exist on Android, and a user
      // got ScalaTest to compile under Android.
      case e if e.getClass.getName == "java.awt.AWTError" => true
      case _ => false
    }
}


}
