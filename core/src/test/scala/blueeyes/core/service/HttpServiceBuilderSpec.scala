package blueeyes.core.service

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import akka.dispatch.Future
import akka.dispatch.Future._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.concurrent.Eventually
import akka.util.Duration
import java.util.concurrent.TimeUnit._

class HttpServiceBuilderSpec extends WordSpec with MustMatchers with MockitoSugar with Eventually {
  "ServiceBuilder startup: creates StartupDescriptor with specified startup function" in{
    var executed = false
    val builder  = new ServiceBuilder[Unit]{
      val descriptor = startup(Future(executed = true))
    }

    val f = builder.descriptor.startup()

    eventually { executed must be (true) }
  }

  "ServiceBuilder startup: creates StartupDescriptor with specified request function" in{
    val function = mock[Function[Unit, AsyncHttpService[Unit]]]
    val builder  = new ServiceBuilder[Unit]{
      val descriptor = request(function)
    }

    builder.descriptor.request()
    verify(builder.descriptor.request, times(1)).apply(())
  }

  "ServiceBuilder shutdown: creates StartupDescriptor with specified shutdown function" in{
    var shutdownCalled = false

    // We need "real" behavior here to allow the Stoppable hooks to run properly
    val builder  = new ServiceBuilder[Unit]{
      val descriptor = shutdown {
        shutdownCalled = true; Future(())
      }
    }

    builder.descriptor.shutdown()

    eventually { shutdownCalled must be (true) }
  }
}
