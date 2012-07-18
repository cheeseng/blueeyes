package blueeyes.health.metrics

import org.scalatest._
import blueeyes.json.JsonAST._

class SampleSpec extends WordSpec with MustMatchers{
  private val sample = new Sample(10)
  "stores raw data" in{
    sample += 1.1
    sample += 2.2
    sample += 2.2

    sample.count                must equal (3)
    sample.rawData.size         must equal (2)
    sample.rawData.get(1.1).get must equal (1)
    sample.rawData.get(2.2).get must equal (2)
  }
  "does not add new data when size is exceeded" in{
    val sample = new Sample(1)
    sample += 1.1
    sample += 2.2

    sample.count                must equal (1)
  }
  "does not add create Histogram when data is not full" in{
    val sample = new Sample(2)
    sample += 1.1

    sample.details              must equal (None)
  }
  "creates Histogram when data is full" in{
    val sample = new Sample(2)
    sample += 1.1
    sample += 2.2

    sample.details              must not equal (None)
  }

  "composes Sample" in{
    val sample = new Sample(1)
    sample += 1.1

    sample.toJValue must equal (JObject(JField("count", JInt(1)) :: JField("histogram", JObject(JField("1", JDouble(1.0)) :: Nil)) :: Nil))
  }
}
