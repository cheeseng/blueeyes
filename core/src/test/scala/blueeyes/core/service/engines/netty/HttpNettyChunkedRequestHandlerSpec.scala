package blueeyes.core.service.engines.netty

import akka.dispatch.Future
import akka.dispatch.Promise
import akka.dispatch.Await
import akka.util.Duration
import blueeyes.bkka.AkkaDefaults
import blueeyes.concurrent.test.AkkaFutures

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.jboss.netty.handler.codec.http.{DefaultHttpRequest, HttpChunk, HttpMethod => NettyHttpMethod, HttpVersion => NettyHttpVersion}
import org.jboss.netty.buffer.{HeapChannelBufferFactory, ChannelBuffers}
import java.net.{SocketAddress, InetSocketAddress}
import blueeyes.core.http.HttpRequest
import org.jboss.netty.channel._
import blueeyes.core.data.{Chunk, ByteChunk}
import collection.mutable.ArrayBuilder.ofByte
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.BeforeAndAfter

class HttpNettyChunkedRequestHandlerSpec extends WordSpec with MustMatchers with MockitoSugar with HttpNettyConverters with BeforeAndAfter with AkkaDefaults with AkkaFutures {

  private val channel       = mock[Channel]
  private val channelConfig = mock[ChannelConfig]
  private val context       = mock[ChannelHandlerContext]
  private val event         = mock[MessageEvent]
  private val remoteAddress = new InetSocketAddress("127.0.0.0", 8080)
  private val handler       = new HttpNettyChunkedRequestHandler(2)
  private val nettyRequest  = new DefaultHttpRequest(NettyHttpVersion.HTTP_1_0, NettyHttpMethod.GET, "/bar/1/adCode.html")

  private val chunkEvent    = mock[MessageEvent]
  private val httpChunk     = mock[HttpChunk]
  private val chunkData     = Array[Byte]('1', '2')

  "NettyChunkedRequestHandler" should {
    "sends requests as it is when it is not chunked" in {
      nettyRequest.setChunked(false)
      handler.messageReceived(context, event)

      verify(context, times(1)).sendUpstream(new UpstreamMessageEventImpl(channel, fromNettyRequest(nettyRequest, remoteAddress), remoteAddress))
      //there was one(context).sendUpstream(new UpstreamMessageEventImpl(channel, fromNettyRequest(nettyRequest, remoteAddress), remoteAddress))
    }

    "sends request and chunk when request is chunked and there is only one chunk" in {
      nettyRequest.setChunked(true)
      when(httpChunk.isLast()) thenReturn (true)

      handler.messageReceived(context, event)
      handler.messageReceived(context, chunkEvent)

      val request: HttpRequest[ByteChunk] = fromNettyRequest(nettyRequest, remoteAddress).copy(content = Some(Chunk(chunkData)))
      verify(context, times(1)).sendUpstream(new UpstreamMessageEventImpl(channel, request, remoteAddress))
    }

    "sends request and chunk when request is chunked and there is only more ther one chunk" ignore {
      nettyRequest.setChunked(true)

      when(httpChunk.isLast()) thenReturn (false)
      handler.messageReceived(context, event)
      handler.messageReceived(context, chunkEvent)

      when(httpChunk.getContent()) thenReturn (ChannelBuffers.wrappedBuffer(chunkData))
      when(httpChunk.isLast()) thenReturn (true)

      handler.messageReceived(context, chunkEvent)

      val nextChunk = Promise.successful[ByteChunk](Chunk(chunkData))
      val request: HttpRequest[ByteChunk] = fromNettyRequest(nettyRequest, remoteAddress).copy(content = Some(Chunk(chunkData, Some(nextChunk))))

      verify(context, times(1)).sendUpstream(new UpstreamMessageEventImpl(channel, request, remoteAddress))
    }
  }

  before {
    when(channel.getConfig()) thenReturn channelConfig
    when(channelConfig.getBufferFactory()) thenReturn HeapChannelBufferFactory.getInstance()
    when(event.getRemoteAddress()) thenReturn remoteAddress
    when(event.getChannel()) thenReturn channel
    when(context.getChannel()) thenReturn channel
    when(event.getMessage()) thenReturn(nettyRequest, Array(nettyRequest))

    when(chunkEvent.getMessage()) thenReturn(httpChunk, Array(httpChunk))
    when(chunkEvent.getChannel()) thenReturn channel
    when(chunkEvent.getRemoteAddress()) thenReturn remoteAddress
    when(httpChunk.getContent()) thenReturn ChannelBuffers.wrappedBuffer(chunkData)
  }

  class UpstreamMessageEventImpl(channel: Channel, message: HttpRequest[ByteChunk], remoteAddress: SocketAddress) extends UpstreamMessageEvent(channel, message, remoteAddress){
    override def equals(p1: Any) = {
      val anotherEvent    = p1.asInstanceOf[UpstreamMessageEvent]
      val anotherMessage  = anotherEvent.getMessage.asInstanceOf[HttpRequest[ByteChunk]]

      val b = anotherEvent.getChannel == channel
      val b1 = anotherMessage.copy(content = None) == message.copy(content = None)
      val b2 = message.content.map(readContent(_)) == anotherMessage.content.map(readContent(_))
      val b3 = anotherEvent.getRemoteAddress == remoteAddress
      b && b1 &&
      b2 && b3
    }

    private def readContent(chunk: ByteChunk): String = new String(readContent(chunk, new ofByte()).result)
    private def readContent(chunk: ByteChunk, buffer: ofByte): ofByte = {
      buffer ++= chunk.data

      val next = chunk.next
      next match {
        case None =>  buffer
        case Some(x) => readContent(Await.result(x, Duration(10, "seconds")), buffer)
      }
    }
  }
}
