package blueeyes.core.service.engines

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.streum.configrity.Configuration 
import org.streum.configrity.io.BlockFormat
import java.net.{InetSocketAddress, InetAddress}

class InetInterfaceLookupSpec extends WordSpec with MustMatchers {

  "creates socket address when address is not configured" in{
    val config = Configuration.parse("", BlockFormat)

    InetInterfaceLookup.socketAddres(config.detach("server"), 8080) must equal (new InetSocketAddress(8080))
  }
  "creates host name when address is not configured" in{
    val config = Configuration.parse("", BlockFormat)

    InetInterfaceLookup.host(config.detach("server")) must equal (InetAddress.getLocalHost().getHostName())
  }
  "creates socket address when address is configured" in{
    val rawConfig = """
    server {
      address = 192.168.10.10
    }
    """

    val config = Configuration.parse(rawConfig, BlockFormat)

    InetInterfaceLookup.socketAddres(config.detach("server"), 8080) must equal (new InetSocketAddress("192.168.10.10", 8080))
  }
  "creates host name when address is configured" in{
    val rawConfig = """
    server {
      address = 192.168.10.10
    }
    """
    val config = Configuration.parse(rawConfig, BlockFormat)

    InetInterfaceLookup.host(config.detach("server")) must equal ("192.168.10.10")
  }
}
