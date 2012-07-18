package blueeyes.health

import org.scalatest._
import blueeyes.json.JsonAST.JInt

class ExportedStatisticSpec extends WordSpec with MustMatchers with blueeyes.json.Implicits{
  "ExportedStatistic: gets lazy value" in{
    var value: Int = 0
    def lazyF: Int = value

    val statistic = new ExportedStatistic(lazyF)

    value = 2

    statistic.details must equal (2)
  }
  "ExportedStatistic: creates JValue" in{
    var value: Int = 0
    def lazyF: Int = value

    val statistic = new ExportedStatistic(lazyF)

    value = 1

    statistic.toJValue must equal (JInt(1))
  }
}
