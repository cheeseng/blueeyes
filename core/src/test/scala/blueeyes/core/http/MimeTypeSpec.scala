package blueeyes.core.http

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import blueeyes.core.http.MimeTypes._

class MimeTypeSpec extends WordSpec with MustMatchers {

  "MimeType:  parsing should parse an example correctly" in {
    val mimeTypeExample = "text/plain; q=0.5, text/html, application/x-dvi; q=0.8, text/x-c, audio/basic"
    val mimeTypeExampleNoQ = "text/plain, text/html, application/x-dvi, text/x-c, audio/basic"
    MimeTypes.parseMimeTypes(mimeTypeExample).map(_.value).mkString(", ") must equal (mimeTypeExampleNoQ)
  }

  "MimeType:  Should output an empty array with input \"game/minecraft\"" in {
    MimeTypes.parseMimeTypes("game/minecraft").length must equal (0)
  }

  "MimeType:  Should output an text/plain from input \"game/minecraft, text/plain\"" in {
    MimeTypes.parseMimeTypes("game/minecraft, text/plain")(0).value must equal ("text/plain")
  }

  "MimeType:  Should produce text/javascript with input \"text/javascript\"" in {
    MimeTypes.parseMimeTypes("text/javascript")(0).value must equal ("text/javascript")
  }

  "MimeType:  Should produce anymaintype/anysubtype with input \"*/*\"" in {
    MimeTypes.parseMimeTypes("*/*")(0).value must equal ("*/*")
  }

  "MimeType:  Should remove q values, producing text/html with \"text/html; q=.6\"" in {
    MimeTypes.parseMimeTypes("text/html; q=.6")(0).value must equal ("text/html")
  }

  "Accept-Type:  Should be able to create a new instance with \"text/html, video/quicktime, application/json\" " in {
    HttpHeaders.Accept(text/html, video/quicktime, application/json).value must equal ("text/html, video/quicktime, application/json")
  }

  "Content-Type:  Should be able to create a new instance with text/html" in {
    HttpHeaders.`Content-Type`(text/html).value must equal ("text/html")
  }

  "Content-Type:  Should be */* with anymaintype/anysubtype" in {
    HttpHeaders.`Content-Type`(anymaintype/anysubtype).value must equal ("*/*")
  }

  "Content-Type:  Should return a Content Type with MimeType inputs" in {
    HttpHeaders.`Content-Type`(MimeTypes.parseMimeTypes("multipart/mixed, application/*"): _*).value must equal ("multipart/mixed, application/*")
  }

}

