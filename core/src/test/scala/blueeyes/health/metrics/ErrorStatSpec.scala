package blueeyes.health.metrics

import org.scalatest._
import blueeyes.json.JsonAST._

class ErrorStatSpec extends WordSpec with MustMatchers{
  "counts errors" in{
    val stats = new ErrorStat()

    stats += new NullPointerException()
    stats += new NullPointerException()

    stats.count must equal (2)
  }
  "creates details" in{
    val stats = new ErrorStat()

    stats += new NullPointerException()
    stats += new NullPointerException()
    stats += new RuntimeException()

    stats.details.get(classOf[NullPointerException]).get must equal (2)
    stats.details.get(classOf[RuntimeException]).get must equal (1)
  }

  "composes ErrorStat" in{
    val stats = new ErrorStat()

    stats += new NullPointerException()
    stats += new NullPointerException()

    stats.toJValue must equal (JObject(JField("errorCount", JInt(2)) :: JField("errorDistribution", JObject(JField(classOf[NullPointerException].getName, JInt(2)) :: Nil)) :: Nil))
  }
}
