package blueeyes.core.service

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class HttpReflectiveServiceListSpec extends WordSpec with MustMatchers {

  "HttpReflectiveServiceList: finds service if it is declared as varuable" in{
    val serviceList = new HttpReflectiveServiceList[Unit]{
      val service = ServiceImpl
    }

    serviceList.services must equal (serviceList.service :: Nil)
  }
  "HttpReflectiveServiceList: finds service if it is declared as methods" in{
    val serviceList = new HttpReflectiveServiceList[Unit]{
      def service = ServiceImpl
    }

    serviceList.services must equal (serviceList.service :: Nil)
  }

  object ServiceImpl extends Service[Unit]{
    def ioClass = null

    def descriptorFactory = null

    def version = ServiceVersion(1, 2, "3")

    def desc = None

    def name = null
  }
}
