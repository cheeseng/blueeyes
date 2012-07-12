package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import blueeyes.core.http.MimeTypes._

class ConnectionTokenSpec extends WordSpec with MustMatchers {

  "Connection:  Should return \"foo\" when passeed \" foo 0r91j2 \\n\"." in {
    HttpHeaders.Connection(ConnectionTokens.parseConnectionTokens(" foo 0r91j2\n ").get).value must equal ("foo")
  }
  "Connection:  Should create a new connection header from \"close\"." in {
    HttpHeaders.Connection(ConnectionTokens.parseConnectionTokens("close").get).value must equal ("close")
  }

  "Connection:  Should create a custom connection header from the depreciated \" Keep-Alive \"." in {    HttpHeaders.Connection(ConnectionTokens.parseConnectionTokens(" Keep-Alive ").get).value must equal ("Keep-Alive")
  }
}
