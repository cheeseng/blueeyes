package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class TrailerSpec extends WordSpec with MustMatchers {

  "Trailer/HttpHeaderFields: Should parse correctly, if parsing for trailer" in {
    HttpHeaders.Trailer(HttpHeaderField.parseAll("Accept, Age, Date, Max-Forwards, Content-Length", "trailer"): _*).value must equal ("Accept, Age, Date, Max-Forwards")
  }

  "Trailer/HttpHeaderFields: Should also prarse correctly if not parsing fo the trailer" in {
    HttpHeaderField.parseAll("Accept, Age, Date, Max-Forwards, Cats, Content-Length", "").map(_.toString).mkString(", ") must equal ("Accept, Age, Date, Max-Forwards, Content-Length")
  }

}

