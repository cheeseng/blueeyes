package blueeyes.persistence.cache

import org.scalatest._
import org.scalatest.time._
import java.util.concurrent.TimeUnit.{MILLISECONDS}
import org.scalatest.concurrent.Eventually

class CacheSpec extends WordSpec with MustMatchers with Eventually{

  override implicit val patienceConfig =
    PatienceConfig(timeout = scaled(Span(3, Seconds)), interval = scaled(Span(100, Millis)))

  "Cache.concurrent: creates new map" in {
    Cache.concurrent(settings()) must not be (null)
  }
  "Cache.concurrent: adds new Entry" in {
    val map = Cache.concurrent(settings())
    map.put("foo", "bar")

    map.get("foo") must be (Some("bar"))
  }

  "Cache.concurrent.put: evict eldest entry" in {
    var evicted = false
    val map = Cache.concurrent(CacheSettings[String, String](ExpirationPolicy(None, None, MILLISECONDS), 1, {(key: String, value: String) => evicted = key == "foo" && value == "bar"}, 1))
    map.put("foo", "bar")
    map.put("baz", "foo")

    evicted        must equal (true)
    map.get("foo") must be (None)
    map.get("baz") must be (Some("foo"))
  }

  "Cache.concurrent.put: evicts when idle time is expired" in{
    val map = Cache.concurrent(settings(Some(50)))
    map.put("baz", "bar")
    eventually { map.contains("baz") must equal (false) }
  }
  "Cache.concurrent.put: evicts when live time is expired" in{
    val map = Cache.concurrent(settings(None, Some(200)))
    map.put("baz", "bar")
    eventually { map.contains("baz")  must equal (false) }
  }
  "Cache.concurrent: evict is called when entry is expired" in{
    var expired = false
    val map = Cache.concurrent(settings(None, Some(200), {(key: String, value: String) => expired = key == "foo" && value == "bar"}))
    map.put("foo", "bar")

    eventually { map.contains("foo") must equal (false) }
    expired must equal (true)
  }
  "Cache.concurrent: adds new Entry 2" in {  // This is identical to the first test, should be removed.
    val map = Cache.concurrent(settings())
    map.put("foo", "bar")

    map.get("foo") must be (Some("bar"))
  }

  private def settings(timeToIdle: Option[Long] = None, timeToLive: Option[Long] = None,
                       evict: (String, String) => Unit = {(key: String, value: String) => }) = CacheSettings[String, String](ExpirationPolicy(timeToIdle, timeToLive, MILLISECONDS), 100, evict)
}
