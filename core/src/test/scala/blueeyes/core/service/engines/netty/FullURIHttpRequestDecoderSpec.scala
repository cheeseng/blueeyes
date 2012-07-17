package blueeyes.core.service.engines.netty

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.jboss.netty.handler.codec.http.{HttpMethod, DefaultHttpRequest, HttpVersion}

class FullURIHttpRequestDecoderSpec extends WordSpec with MustMatchers {
  "creates full uri" in{
    val decoder = new FullURIHttpRequestDecoder("http", "google", 8080, 8192)
    val message = decoder.createMessage(Array("GET", "/foo", "HTTP/1.1")).asInstanceOf[DefaultHttpRequest]

    message.getMethod           must equal (HttpMethod.GET)
    message.getUri              must equal ("http://google:8080/foo")
    message.getProtocolVersion  must equal (HttpVersion.HTTP_1_1)
  }
  "creates full uri when first slash is missing" in{
    val decoder = new FullURIHttpRequestDecoder("http", "google", 8080, 8192)
    val message = decoder.createMessage(Array("GET", "foo", "HTTP/1.1")).asInstanceOf[DefaultHttpRequest]

    message.getUri              must equal ("http://google:8080/foo")
  }
  "creates full uri when original uri is full" in{
    val decoder = new FullURIHttpRequestDecoder("http", "google", 8080, 8192)
    val message = decoder.createMessage(Array("GET", "http://google:8080/foo", "HTTP/1.1")).asInstanceOf[DefaultHttpRequest]

    message.getUri              must equal ("http://google:8080/foo")
  }
}
