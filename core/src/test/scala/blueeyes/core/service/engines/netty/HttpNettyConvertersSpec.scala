package blueeyes.core.service.engines.netty

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

import org.jboss.netty.handler.codec.http.{HttpResponseStatus, HttpMethod => NettyHttpMethod, HttpVersion => NettyHttpVersion, DefaultHttpRequest}
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}
import org.jboss.netty.util.CharsetUtil
import java.net.InetSocketAddress
import blueeyes.core.http._
;
import scala.collection.JavaConversions._

import blueeyes.core.http.HttpVersions._
import blueeyes.core.data.{ByteChunk, BijectionsChunkString}
import blueeyes.core.http.MimeTypes._

class HttpNettyConvertersSpec extends WordSpec with MustMatchers with HttpNettyConverters with BijectionsChunkString{
  "convert netty method to service method" in {
    fromNettyMethod(NettyHttpMethod.GET) must equal (HttpMethods.GET)
  }
  "convert netty version to service version" in {
    fromNettyVersion(NettyHttpVersion.HTTP_1_1) must equal (`HTTP/1.1`)
  }
  "convert service version to netty version" in {
    toNettyVersion(`HTTP/1.1`) must equal(NettyHttpVersion.HTTP_1_1)
  }
  "convert service HttpStatus to netty HttpStatus" in {
    toNettyStatus(HttpStatus(HttpStatusCodes.NotFound, "missing")) must equal (new HttpResponseStatus(HttpStatusCodes.NotFound.value, "missing"))
  }
  "convert service HttpResponse to netty HttpResponse" in {
    val response = HttpResponse[ByteChunk](HttpStatus(HttpStatusCodes.NotFound), Map("Retry-After" -> "1"), Some(StringToChunk("12")), `HTTP/1.0`)
    val message  = toNettyResponse(response, true)

    message.getStatus                               must equal (new HttpResponseStatus(HttpStatusCodes.NotFound.value, ""))
    message.getProtocolVersion                      must equal (NettyHttpVersion.HTTP_1_0)
    Map(message.getHeaders.map(header => (header.getKey, header.getValue)): _*)  must equal (Map("Retry-After" -> "1", "Transfer-Encoding" -> "chunked"))

  }
  "convert service HttpResponse to netty HttpResponse with not chunked content" in {
    val response = HttpResponse[ByteChunk](HttpStatus(HttpStatusCodes.NotFound), Map(), None, `HTTP/1.0`)
    val message  = toNettyResponse(response, false)

    message.getStatus                               must equal (new HttpResponseStatus(HttpStatusCodes.NotFound.value, ""))
    message.getProtocolVersion                      must equal (NettyHttpVersion.HTTP_1_0)
    Map(message.getHeaders.map(header => (header.getKey, header.getValue)): _*)  must equal (Map("Content-Length" -> "0"))
  }

  "convert netty NettyHttpRequest to service NettyHttpRequest" in {
    val nettyRequest  = new DefaultHttpRequest(NettyHttpVersion.HTTP_1_0, NettyHttpMethod.GET, "http://foo/bar%20foo?param1=foo%20bar")
    nettyRequest.setContent(ChannelBuffers.wrappedBuffer("12".getBytes))
    nettyRequest.setHeader("Retry-After", "1")

    val address = new InetSocketAddress("127.0.0.0", 8080)
    val request = fromNettyRequest(nettyRequest, address)

    request.method       must equal (HttpMethods.GET)
    request.parameters   must equal (Map('param1 -> "foo bar"))
    request.uri          must equal (URI("http://foo/bar%20foo?param1=foo%20bar"))
    request.headers.raw  must equal (Map("Retry-After" -> "1"))
    request.version      must equal (`HTTP/1.0`)
    request.remoteHost   must equal (Some(address.getAddress))
  }

  "convert netty NettyHttpRequest to service NettyHttpRequest, modifying ip if X-Forwarded-For header present" in {
    val nettyRequest  = new DefaultHttpRequest(NettyHttpVersion.HTTP_1_0, NettyHttpMethod.GET, "http://foo/bar?param1=value1")
    nettyRequest.setContent(ChannelBuffers.wrappedBuffer("12".getBytes))
    nettyRequest.setHeader("Retry-After", "1")
    nettyRequest.setHeader("X-Forwarded-For", "111.11.11.1, 121.21.2.2")

    val address = new InetSocketAddress("127.0.0.0", 8080)
    val forwardedAddress = new InetSocketAddress("111.11.11.1", 8080)
    val request = fromNettyRequest(nettyRequest, address)

    request.method      must equal (HttpMethods.GET)
    request.uri         must equal (URI("http://foo/bar?param1=value1"))
    request.parameters  must equal (Map('param1 -> "value1"))
    request.headers.raw must equal (Map("Retry-After" -> "1", "X-Forwarded-For" -> "111.11.11.1, 121.21.2.2"))
    request.version     must equal (`HTTP/1.0`)
    request.remoteHost  must equal (Some(forwardedAddress.getAddress))
  }
  "convert netty NettyHttpRequest to service NettyHttpRequest, modifying ip if X-Cluster-Client-Ip header present" in {
    val nettyRequest  = new DefaultHttpRequest(NettyHttpVersion.HTTP_1_0, NettyHttpMethod.GET, "http://foo/bar?param1=value1")
    nettyRequest.setHeader("X-Cluster-Client-Ip", "111.11.11.1, 121.21.2.2")

    val address = new InetSocketAddress("127.0.0.0", 8080)
    val forwardedAddress = new InetSocketAddress("111.11.11.1", 8080)
    val request = fromNettyRequest(nettyRequest, address)

    request.method      must equal (HttpMethods.GET)
    request.uri         must equal (URI("http://foo/bar?param1=value1"))
    request.headers.raw must equal (Map("X-Cluster-Client-Ip" -> "111.11.11.1, 121.21.2.2"))
    request.remoteHost  must equal (Some(forwardedAddress.getAddress))
  }

  "does not use host name from Host header" in {
    val nettyRequest  = new DefaultHttpRequest(NettyHttpVersion.HTTP_1_0, NettyHttpMethod.GET, "http://foo/bar?param1=value1")
    nettyRequest.addHeader("Host", "google.com")

    val request = fromNettyRequest(nettyRequest, new InetSocketAddress("127.0.0.0", 8080))

    request.uri must equal (URI("http://foo/bar?param1=value1"))
  }

  "convert netty NettyHttpRequest with multiple headers values to service HttpRequest" in {
    val nettyRequest  = new DefaultHttpRequest(NettyHttpVersion.HTTP_1_0, NettyHttpMethod.GET, "http://foo/bar?param1=value1")
    nettyRequest.addHeader("TCodings", "1")
    nettyRequest.addHeader("TCodings", "2")

    val request = fromNettyRequest(nettyRequest, new InetSocketAddress("127.0.0.0", 8080))

    request.headers.raw must equal (Map("TCodings" -> "1,2"))
  }
}
