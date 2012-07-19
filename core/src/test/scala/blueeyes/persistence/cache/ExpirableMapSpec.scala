package blueeyes.persistence.cache

import org.scalatest._
import org.scalatest.concurrent.Eventually
import java.util.concurrent.{ConcurrentHashMap, Executors}
import java.util.concurrent.TimeUnit.{MILLISECONDS}

class ExpirableMapSpec extends WordSpec with MustMatchers with Eventually {
  "ExpirableMap: containsKey is 'true' when entry is valid" in{
    val map = newMap()

    map.containsKey("foo") must equal (true)
  }
  "ExpirableMap: containsKey is 'false' when entry is not valid" in{
    val map = newMap()

    map.containsKey("bar") must equal (false)
  }
  "ExpirableMap: containsKey is 'false' when entry is expired" in{
    val map = newMap(None, None, {hasExpired: Expirable[String, String] => true})

    map.containsKey("bar") must equal (false)
  }
  "ExpirableMap: evict is called when entry is expired by containsKey" in{
    var expired = false
    val map = newMap(None, None, {hasExpired: Expirable[String, String] => true}, {(key: String, value: String) => expired = key == "foo" && value == "bar"})
    map.containsKey("foo")

    expired must equal (true)
  }
  "ExpirableMap: evict is called when entry is expired by put" in{
    var expired = false
    val map = newMap(None, None, {hasExpired: Expirable[String, String] => true}, {(key: String, value: String) => expired = key == "baz" && value == "bar"})
    map.put("baz", "bar")
    map.put("baz", "foo")

    expired must equal (true)
  }
  "ExpirableMap: evict is called when entry is expired by replace" in{
    var expired = false
    val map = newMap(None, None, {hasExpired: Expirable[String, String] => true}, {(key: String, value: String) => expired = key == "foo" && value == "bar"})
    map.replace("foo", "bar")

    expired must equal (true)
    map.get("foo") must be (None)
  }
  "ExpirableMap: get is Some when entry is valid" in{
    val map = newMap()

    map.get("foo") must be (Some("bar"))
  }
  "ExpirableMap: get is None when entry is not valid" in{
    val map = newMap()

    map.get("bar") must be (None)
  }
  "ExpirableMap: get is None when entry is expired" in{
    val map = newMap(None, None, {hasExpired: Expirable[String, String] => true})

    map.get("bar") must be (None)
  }
  "ExpirableMap.putIfAbsent: replace expired value" in{
    val map = newMap(None, None, {hasExpired: Expirable[String, String] => hasExpired.value == "bar"})
    map.putIfAbsent("foo", "baz")

    map.get("foo") must be (Some("baz"))
  }
  "ExpirableMap.putIfAbsent: does not replace value" in{
    val map = newMap()
    map.putIfAbsent("foo", "baz")

    map.get("foo") must be (Some("bar"))
  }
  "ExpirableMap.put: adds new value" in{
    val map = newMap()
    map.putIfAbsent("baz", "foo")

    map.get("baz") must be (Some("foo"))
  }
  "ExpirableMap.replace: replaces value" in{
    val map = newMap()
    map.replace("foo", "baz") must be (Some("bar"))

    map.get("foo") must be (Some("baz"))
  }
  "ExpirableMap.replace: does not replace not existing value" in{
    val map = newMap()
    map.replace("bar", "baz") must be (None)

    map.get("bar") must be (None)
  }
  "ExpirableMap.remove: removes value" in{
    val map = newMap()
    map.remove("foo") must be (Some("bar"))

    map.get("foo") must be (None)
  }
  "ExpirableMap: evict is called when entry is expired by get" in{
    var expired = false
    val map = newMap(None, None, {hasExpired: Expirable[String, String] => true}, {(key: String, value: String) => expired = key == "foo" && value == "bar"})
    map.get("foo")

    expired must equal (true)
  }
  "ExpirableMap: evict is called when entry is expired by remove" in{
    var expired = false
    val map = newMap(None, None, {hasExpired: Expirable[String, String] => true}, {(key: String, value: String) => expired = key == "foo" && value == "bar"})
    map.remove("foo")

    expired must equal (true)
  }
  "ExpirableMap: containsValue is 'true' when entry is valid" in{
    val map = newMap()

    map.containsValue("bar") must equal (true)
  }
  "ExpirableMap: containsValue is 'false' when entry is not valid" in{
    val map = newMap()

    map.containsValue("foo") must equal (false)
  }
  "ExpirableMap: containsValue is 'false' when entry is expired" in{
    val map = newMap(None, None, {hasExpired: Expirable[String, String] => true})

    map.containsKey("bar") must equal (false)
  }
  "ExpirableMap: evict is called when entry is expired by containsValue" in{
    var expired = false
    val map = newMap(None, None, {hasExpired: Expirable[String, String] => true}, {(key: String, value: String) => expired = key == "foo" && value == "bar"})
    map.containsValue("bar")

    expired must equal (true)
  }
  "ExpirableMap: putIfAbsent evicts when idle time is expired" in{
    val map = newMap(Some(50), None, {hasExpired: Expirable[String, String] => true})
    eventually { map.containsValue("bar") must equal (false) }
  }
  "ExpirableMap: putIfAbsent evicts when live time is expired" in{
    val map = newMap(None, Some(500), {hasExpired: Expirable[String, String] => true})
    eventually { map.containsValue("bar")  must equal (false) }
  }
  "ExpirableMap.put: evicts when idle time is expired" in{
    val map = newMap(Some(50), None, {hasExpired: Expirable[String, String] => true})
    map.put("baz", "bar")
    eventually { map.containsValue("baz") must equal (false) }
  }
  "ExpirableMap.put: evicts when live time is expired" in{
    val map = newMap(None, Some(500), {hasExpired: Expirable[String, String] => true})
    map.put("baz", "bar")
    eventually { map.containsValue("baz") must equal (false) }
  }
  "ExpirableMap.replace: evicts when idle time is expired" in{
    val map = newMap(Some(50), None, {hasExpired: Expirable[String, String] => hasExpired.value == "baz"})
    map.replace("foo", "baz") must be (Some("bar"))
    eventually { map.containsValue("foo") must equal (false) }
  }
  "ExpirableMap.replace: evicts when live time is expired" in{
    val map = newMap(None, Some(500), {hasExpired: Expirable[String, String] => hasExpired.value == "baz"})
    map.replace("foo", "baz") must be (Some("bar"))
    eventually { map.containsValue("foo")  must equal (false) }
  }

  private def newMap(timeToIdle: Option[Long] = None, timeToLive: Option[Long] = None, hasExpired: Expirable[String, String] => Boolean = {hasExpired: Expirable[String, String] => false},
                    evict: (String, String) => Unit = {(key: String, value: String) => }) = {
    val map = new ExpirableMap[String, String](new ConcurrentHashMap[String, Expirable[String,String]](),
              ExpirationPolicy(timeToIdle, timeToLive, MILLISECONDS), hasExpired,
              Executors.newScheduledThreadPool(2), evict)
    map.putIfAbsent("foo", "bar")
    map
  }
}
