package blueeyes.health

import metrics.{IntervalLength, eternity, interval}
import org.scalatest._
import blueeyes.json.JPathImplicits._
import akka.dispatch.Future
import akka.dispatch.Promise
import blueeyes.concurrent.test.AkkaFutures
import blueeyes.json.JsonAST._
import blueeyes.json.{JsonParser, Printer, JPath}
import java.util.concurrent.TimeUnit
import org.scalatest.concurrent.Eventually

class IntervalHealthMonitorSpec extends WordSpec with MustMatchers with Eventually with blueeyes.json.Implicits with blueeyes.bkka.AkkaDefaults 
with AkkaFutures {
  implicit val healthMonitorTimeout = akka.util.Timeout(10000)

  private val monitor = new IntervalHealthMonitor(eternity)

  "records count" in{
    monitor.increment("foo")(2)
    monitor.increment("foo")(3)

    monitor.countStats.size must equal (1)
    monitor.countStats.get(JPath("foo")).get.count.futureValue must equal (5)
  }

  "records the duration of the event" in {
    monitor.time("foo")({ Thread.sleep(10) })
    monitor.timerStats.size must equal (1)
  }
  "records errors" in {
    monitor.error("foo")(new NullPointerException())
    monitor.errorStats.size must equal (1)
  }

  "monitors future time" in {
    monitor.monitor("foo")(Future({ Thread.sleep(10) }))
    eventually { monitor.timerStats.size must equal (1) }
  }

  "monitors future error" in {
    val promise = Promise[Unit]()
    monitor.monitor("foo")(promise)
    
    promise.failure(new NullPointerException())

    monitor.errorStats.size must equal (1)
  }

  "records sample event" in {
    monitor.sample("foo")(1.1)
    monitor.sampleStats.size must equal (1)
    monitor.sampleStats.get(JPath("foo")).get.count must equal (1)
  }

  "traps error" in {
    try {
      monitor.trap("foo") {throw new NullPointerException()}
    } catch {
      case t: Throwable =>
    }

    monitor.errorStats.size must equal (1)
  }

  "composes errors into JValue as array" in{
    val config  = blueeyes.health.metrics.interval(IntervalLength(1, TimeUnit.SECONDS), 3)
    val monitor = new IntervalHealthMonitor(config)
    monitor.error("foo")(new NullPointerException())
    Thread.sleep(900)

    val monitorJson = JsonParser.parse("""{"foo":{"errorDistribution":{"java.lang.NullPointerException":{"1s x 3":[1,0,0]}},"count":{"1s x 3":[1,0,0]}}}""")
    val jValue = monitor.toJValue
    jValue.futureValue must equal (monitorJson)
  }

  "composes into JValue" in{

    def export: Int = 2

    val config  = eternity
    val monitor = new IntervalHealthMonitor(config)
    monitor.increment("requestCount")(2)
    monitor.increment("requestCount")(3)
    monitor.export("request.export", export)

    val monitorJson = JObject(List(JField("requestCount", JObject(JField(config.toString, JArray(JInt(5) :: Nil)) :: Nil)), JField("request", JObject(List(JField("export", JInt(2)))))))
    monitor.toJValue.futureValue must equal (monitorJson)
  }
}
