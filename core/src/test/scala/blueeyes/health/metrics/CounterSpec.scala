package blueeyes.health.metrics

import blueeyes.json.JsonAST.JInt
import org.scalatest._

class CounterSpec extends WordSpec with MustMatchers {
  "a counter of zero" should {
    def makeCounter = new Counter(0)

    "incremented by one" in {
      val counter = makeCounter
      counter  += 1

      counter.count must equal (1)
    }

    "incremented by two" in {
      val counter = makeCounter
      counter += 2

      counter.count must equal (2)
    }

    "composes Counter" in{
      val counter = new Counter(0)
      counter += 2

      counter.toJValue must equal (JInt(2))
    }
  }

  "a counter without an explicit initial value" should {
    "equals one" in {
      new Counter().count must equal (0)
    }
  }
}
