package blueeyes.health.metrics

import blueeyes.json.JsonAST._
import org.scalatest._

class TimedEternityAverageStatSpec extends WordSpec with MustMatchers with TimedStatFixtures with blueeyes.concurrent.test.AkkaFutures {
  "TimedEternityAverageStat" should{
    "creates JValue" in{
      val timedSample = TimedAverageStat(eternity)
      fill(timedSample)

      val histogramValue = JArray(List(JDouble(4)))
      timedSample.toJValue.futureValue must equal (JObject(JField("perSecond", JObject(JField(eternity.toString, histogramValue) :: Nil)) :: Nil))
    }
  }

  private def fill(timedSample: Statistic[Long]){
    set(timedSample, 1001)
    set(timedSample, 1001)
    set(timedSample, 1002)
    set(timedSample, 1002)
  }

  private def set(timedSample: Statistic[Long], now: Long) = {
    clock.setNow(now)
    timedSample += 1
  }
}
