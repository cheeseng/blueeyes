package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class HttpDateTimeSpec extends WordSpec with MustMatchers {

  "Date:  Should return an HttpDate object with correct inputs" in {
    HttpHeaders.Date(HttpDateTimes.parseHttpDateTimes("  MON, 01-JAN-2001 00:00:00 UTC  ").get).value must equal ("Mon, 01 Jan 2001 00:00:00 GMT")
  }
  
  "Date: Should return an HttpDate object given correct inputs with suspect capitalization" in {
    HttpHeaders.Date(HttpDateTimes.parseHttpDateTimes("tue, 29 dec 2009 12:12:12 GMT  ").get).value must equal ("Tue, 29 Dec 2009 12:12:12 GMT")
  }

  "Date:  Should return none for badly formatted date" in {
    HttpDateTimes.parseHttpDateTimes("Mon, 01-Jan-2001 00:00:00 UTC fooo baaaaar") must equal (None)
  }

}

