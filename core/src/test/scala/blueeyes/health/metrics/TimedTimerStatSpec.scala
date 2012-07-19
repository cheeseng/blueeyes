package blueeyes.health.metrics

import org.scalatest._
import blueeyes.json.JsonAST._
import blueeyes.json.Printer
import java.util.concurrent.TimeUnit

class TimedTimerStatSpec extends WordSpec with MustMatchers with TimedStatFixtures with blueeyes.concurrent.test.AkkaFutures {
  "TimedTimerStat" should{
    "creates JValue" in{
      val config = blueeyes.health.metrics.interval(IntervalLength(3, TimeUnit.SECONDS), 3)
      val timedSample = TimedTimerStat(config)
      fill(timedSample)

      val values = ("minimumTime", List(JDouble(1.0E-6), JDouble(1.0E-6), JDouble(0.0))) :: ("maximumTime", List(JDouble(1.0E-6), JDouble(1.0E-6), JDouble(0.0))) :: ("averageTime", List(JDouble(1.0E-6), JDouble(1.0E-6), JDouble(0.0))) :: ("standardDeviation", List(JDouble(0.0), JDouble(0.0), JDouble(0.0))) :: Nil
      val jValue = timedSample.toJValue
      jValue.futureValue must equal (JObject(values.map(kv => JField(kv._1, JObject(JField(config.toString, JArray(kv._2)) :: Nil)))))
    }

    "creates TimedSample if the configuration is interval" in{
      TimedTimerStat(blueeyes.health.metrics.interval(IntervalLength(3, TimeUnit.SECONDS), 7)).isInstanceOf[TimedSample[_]] must be (true)
      //TimedTimerStat(interval(IntervalLength(3, TimeUnit.SECONDS), 7)) must beAnInstanceOf[TimedSample[_]] 
    }

    "creates EternityTimedSample if the configuration is eternity" in{
      TimedTimerStat(eternity).isInstanceOf[EternityTimedTimersSample] must be (true)
      //TimedTimerStat(eternity) must beAnInstanceOf[EternityTimedTimersSample] 
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

    Thread.sleep(50)
  }
}
