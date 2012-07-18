package blueeyes.health.metrics

import org.scalatest._
import java.util.concurrent.TimeUnit

class IntervalParserSpec extends WordSpec with MustMatchers{
  "IntervalParser" should{
    "parse 'seconds' interval" in{
      IntervalParser.parse("30s x 10") must equal (interval(IntervalLength(30, TimeUnit.SECONDS), 10))
      IntervalParser.parse("3 s x 10") must equal (interval(IntervalLength(3, TimeUnit.SECONDS), 10))
    }
    "parse 'minutes' interval" in{
      IntervalParser.parse("30min x 10") must equal (interval(IntervalLength(30, TimeUnit.MINUTES), 10))
      IntervalParser.parse("3 min x 10") must equal (interval(IntervalLength(3, TimeUnit.MINUTES), 10))
    }
    "parse 'hours' interval" in{
      IntervalParser.parse("3h x 10") must equal (interval(IntervalLength(3, TimeUnit.HOURS), 10))
      IntervalParser.parse("30 h x 10") must equal (interval(IntervalLength(30, TimeUnit.HOURS), 10))
    }
    "parse 'eternity' interval" in{
      IntervalParser.parse("eternity") must equal (eternity)
    }
  }
}
