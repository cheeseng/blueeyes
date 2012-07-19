package blueeyes.persistence.cache

import org.scalatest._
import java.util.concurrent.TimeUnit.{NANOSECONDS, MILLISECONDS}
import java.lang.System.{nanoTime}

class ExpirableSpec extends WordSpec with MustMatchers{
  private val expirable = Expirable("foo", "bar", ExpirationPolicy(None, None, NANOSECONDS))

  "Expirable: records access time" in{

    val lower = nanoTime()
    expirable.value
    val upper = nanoTime()

    expirable.accessTime(NANOSECONDS) must be >= (lower)
    expirable.accessTime(NANOSECONDS) must be <= (upper)
  }

  "Expirable: can convert access time" in{
    expirable.accessTime(MILLISECONDS) must equal (MILLISECONDS.convert(expirable.accessTimeNanos, NANOSECONDS))
  }
  "Expirable: can convert creation time" in{
    expirable.creationTime(MILLISECONDS) must equal (MILLISECONDS.convert(expirable.creationTimeNanos, NANOSECONDS))
  }
}
