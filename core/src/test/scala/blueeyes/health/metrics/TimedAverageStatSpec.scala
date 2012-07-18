package blueeyes.health.metrics

import org.scalatest._
import blueeyes.json.JsonAST._
import java.util.concurrent.TimeUnit

class TimedAverageStatSpec extends WordSpec with MustMatchers with TimedStatFixtures with blueeyes.concurrent.test.AkkaFutures {
  implicit val healthMonitorTimeout = akka.util.Timeout(10000)

  "TimedAverageStat" should{
    "creates JValue" in{
      val config = blueeyes.health.metrics.interval(IntervalLength(3, TimeUnit.SECONDS), 3)
      val timedSample = TimedAverageStat(config)
      fill(timedSample)

      val histogram      = timedSample.toJValue
      val histogramValue = JArray(List(JDouble(1.3333333333333333), JDouble(1.0), JDouble(0.0)))
      histogram.futureValue must equal (JObject(JField("perSecond", JObject(JField(config.toString, histogramValue) :: Nil)) :: Nil))
      //histogram must whenDelivered (be_==(JObject(JField("perSecond", JObject(JField(config.toString, histogramValue) :: Nil)) :: Nil)))
    }
  }

  private def fill(timedSample: Statistic[Long]){
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

    Thread.sleep(50)
  }
}
