package blueeyes.health.metrics

import org.scalatest._
import blueeyes.json.JsonAST._
import java.util.concurrent.TimeUnit

class TimedCountStatSpec extends WordSpec with MustMatchers with TimedStatFixtures with blueeyes.concurrent.test.AkkaFutures {
  "TimedCountStat" should{
    "creates JValue" in{
      val config      = blueeyes.health.metrics.interval(IntervalLength(3, TimeUnit.SECONDS), 3)
      val timedSample = TimedCountStat(config)
      fill(timedSample)

      val jValue = timedSample.toJValue
      jValue.futureValue must equal (JObject(JField(config.toString, (JArray(List(JInt(4), JInt(3), JInt(0))))) :: Nil))
    }
  }

  private def fill(timedSample: Statistic[Long]){
    set(timedSample, 100000)
    set(timedSample, 101000)
    set(timedSample, 102000)
    set(timedSample, 102100)

    set(timedSample, 103000)
    set(timedSample, 104000)
    set(timedSample, 104020)

    set(timedSample, 112000)
    set(timedSample, 112100)
    set(timedSample, 112020)

    set(timedSample, 114000)
    set(timedSample, 114100)
    set(timedSample, 114020)
    set(timedSample, 115000)
    set(timedSample, 118000)
  }

  private def set(timedSample: Statistic[Long], now: Long) = {
    clock.setNow(now)
    timedSample += 1
  }
}
