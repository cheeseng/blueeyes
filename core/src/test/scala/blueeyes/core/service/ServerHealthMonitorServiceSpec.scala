package blueeyes.core.service

import test.BlueEyesServiceSpec
import blueeyes.json.JsonAST._
import blueeyes.core.http.HttpStatus
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.http.MimeTypes._
import blueeyes.core.http.test.HttpRequestCheckers 

class ServerHealthMonitorServiceSpec extends BlueEyesServiceSpec with ServerHealthMonitorService with HttpRequestCheckers {
  val healthMonitorQueryTimeout = akka.util.Timeout(10000)

   "Server Health Monitor Service" should{
    "get server health" in {
      val content = service.get[JValue]("/blueeyes/server/health").futureValue.content.get
      content \ "runtime" must not equal JNothing
      content \ "memory" must not equal JNothing
      content \ "threads" must not equal JNothing
      content \ "operatingSystem" must not equal JNothing
      content \ "server" \ "hostName" must not equal JNothing
      content \ "server" \ "port" must not equal JNothing
      content \ "server" \ "sslPort" must not equal JNothing
    }
  }
}
