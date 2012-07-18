package blueeyes.health.metrics

import blueeyes.json.JsonAST._
import org.scalatest._

class TimedEternityCountStatSpec extends WordSpec with MustMatchers with TimedStatFixtures with blueeyes.concurrent.test.AkkaFutures {
  implicit val healthMonitorTimeout = akka.util.Timeout(10000)

  "EternityTimedCountStat" should{
    "creates JValue" in{
      val timedSample = TimedCountStat(eternity)
      fill(timedSample)
      timedSample.toJValue.futureValue must equal (JObject(JField(eternity.toString, JArray(List(JInt(4)))) :: Nil))
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
