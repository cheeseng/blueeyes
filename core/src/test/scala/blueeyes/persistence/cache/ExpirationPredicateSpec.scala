package blueeyes.persistence.cache

import org.scalatest._
import org.scalatest.concurrent.Eventually
import java.util.concurrent.TimeUnit.{MILLISECONDS}

class ExpirationPredicateSpec extends WordSpec with MustMatchers with Eventually {
  private val predicate = Expirable.expirationCheck[String, String]

  "ExpirationPredicate: 'false' when policy is 'eternal'" in{
    val expirable = Expirable("foo", "bar", ExpirationPolicy(None, None, MILLISECONDS))

    eventually { predicate(expirable) must equal (false) }
  }
  "ExpirationPredicate: 'true' when Idle time is expired" in{
    val expirable = Expirable("foo", "bar", ExpirationPolicy(Some(1), None, MILLISECONDS))

    eventually { predicate(expirable) must equal (true) }
  }
  "ExpirationPredicate: 'true' when live time is expired" in{
    val expirable = Expirable("foo", "bar", ExpirationPolicy(None, Some(1), MILLISECONDS))

    eventually { predicate(expirable) must equal (true) }
  }
}
