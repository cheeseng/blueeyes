package blueeyes.health.metrics

import akka.dispatch.Future
import akka.dispatch.Promise
import blueeyes.util.metrics.Duration
import Duration._
import org.scalatest._
import java.util.concurrent.TimeUnit

class TimerTest extends WordSpec with MustMatchers with blueeyes.bkka.AkkaDefaults {
  val precision = 5.0 // milliseconds

  "timing an event" should {
    "returns the event's value" in {
      val timer = new Timer
      timer.time { 1 + 1 } must equal (2)
    }

    "records the duration of the event" in {
      val timer = new Timer
      timer.time { Thread.sleep(10) }
      timer.mean.ms.time must not equal (0.0)
    }

    "records the duration of the event specified by future" in {
      val timer  = new Timer
      val promise = Promise[Unit]()

      timer.time(promise)

      Thread.sleep(10)
      promise.success(())

      timer.mean.ms.time must not equal (0.0)
    }

    "records the existence of the event" in {
      val timer = new Timer
      timer.time { Thread.sleep(10) }

      timer.count must equal (1)
    }
  }

  "a blank timer" should {
    val timer = new Timer

    "has a max of zero" in {
      timer.max.ms.time must be (0.0 plusOrMinus precision)
    }

    "has a min of zero" in {
      timer.min.ms.time must be (0.0 plusOrMinus precision)
    }

    "has a mean of zero" in {
      timer.mean.ms.time must be (0.0 plusOrMinus precision)
    }

    "has a standard deviation of zero" in {
      timer.standardDeviation.ms.time must be (0.0 plusOrMinus precision)
    }

    "has a count of zero" in {
      timer.count must equal (0)
    }
  }

  "timing a series of events" should {
    val timer = new Timer
    timer ++= List(
      Duration(10, TimeUnit.MILLISECONDS),
      Duration(20, TimeUnit.MILLISECONDS),
      Duration(20, TimeUnit.MILLISECONDS),
      Duration(30, TimeUnit.MILLISECONDS),
      Duration(40, TimeUnit.MILLISECONDS)
    )

    "calculates the maximum duration" in {
      timer.max.ms.time must be (40.0 plusOrMinus precision)
    }

    "calculates the minimum duration" in {
      timer.min.ms.time must be (10.0 plusOrMinus precision)
    }

    "calculates the mean" in {
      timer.mean.ms.time must be (24.0 plusOrMinus precision)
    }

    "calculates the standard deviation" in {
      timer.standardDeviation.ms.time must be (11.4 plusOrMinus precision)
    }

    "records the count" in {
      timer.count must equal (5)
    }
  }

  "timing crazy-variant values" should {
    val timer = new Timer
    timer ++= List(
      Duration(Long.MaxValue, TimeUnit.MILLISECONDS),
      Duration(0, TimeUnit.MILLISECONDS)
    )

    "calculates the standard deviation without overflowing" in {
      timer.standardDeviation.ms.time must be (6.521908912666392E12 plusOrMinus 1E3)
    }
  }
}
