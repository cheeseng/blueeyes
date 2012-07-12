package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class HttpIpSpec extends WordSpec with MustMatchers {

  "HttpIp: Should recover the ip from \"111.11.11.1\" "in {
    HttpIps.parseHttpIps("111.11.11.1")(0).toString must equal ("111.11.11.1")
  }

  "X-Forwarded-For: Should recover the relevant ips from \"111.11.11.1, 244.11.223.1, cat\" " in {
    HttpHeaders.`X-Forwarded-For`(HttpIps.parseHttpIps("111.11.11.1, 244.11.233.1, cat"): _*).value must equal ("111.11.11.1, 244.11.233.1")
  }
}
