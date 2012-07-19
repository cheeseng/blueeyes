package blueeyes.health.metrics.histogram

import blueeyes.util.metrics.Duration._
import org.scalatest._

import ValueStrategy._
import blueeyes.health.metrics.Timer

class TimerHistogramSpec extends WordSpec with MustMatchers {
  "TimerHistogram" should{
    "create Timer Histogram" in{
      val histogram = new StaticHistogram[Timer](new DynamicLengthBucketsStrategy()).histogram(Map(1.2 -> 2l, 1.5 -> 2l, 5.9 -> 1l, 12.1 -> 3l).toList.sortWith((e1, e2) => (e1._1 < e2._1)))
      histogram.get(1).get.count must equal (2)
      histogram.get(1).get.min must equal (2.nanoseconds)

      histogram.get(4).get.count must equal (1)
      histogram.get(4).get.min must equal (1.nanoseconds)

      histogram.get(7).get.count must equal (0)
      histogram.get(7).get.min must equal (0.nanoseconds)

      histogram.get(10).get.count must equal (1)
      histogram.get(10).get.min must equal (3.nanoseconds)
    }
  }
}
