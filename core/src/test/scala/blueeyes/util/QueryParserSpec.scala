package blueeyes.util

import QueryParser._
import java.net.URI
import org.scalatest._
import java.net.URLEncoder._

class QueryParserSpec extends WordSpec with MustMatchers {
  val baseURI = "http://www.socialmedia.com/test?"
  val encoding = "UTF-8"

  "Support 'normal' query params" in {
    val queryParams = "a=1&b=2"
    val query = URI.create(baseURI + queryParams).getRawQuery()
    var params = parseQuery(query)
    params must contain key ('a)
    params must contain key ('b)
    params('a) must equal ("1")
    params('b) must equal ("2")
    unparseQuery(params) must equal (queryParams)
  }

  "Support value-less query params" in {
    val queryParams = "usermode"
    val query = URI.create(baseURI + queryParams).getRawQuery()
    var params = parseQuery(query)
    params must contain key ('usermode)
    unparseQuery(params) must equal (queryParams)
  }

  "Support empty query string" in {
    val queryParams = ""
    val query = URI.create(baseURI + queryParams).getRawQuery()
    var params = parseQuery(query)
    params.size must equal (0)
    unparseQuery(params) must equal (queryParams)
  }

  "Support query string with fragment appended" in {
    val queryParams = "flag=true"
    val query = URI.create(baseURI + queryParams + "#fragment").getRawQuery()
    var params = parseQuery(query)
    params must contain key ('flag)
    params('flag) must equal ("true")
    unparseQuery(params) must equal (queryParams)
  }

  "Support query string with <space>" in {
    val queryParams = "flag=true&path=" + encode("/hello world", encoding)
    val query = URI.create(baseURI + queryParams).getRawQuery
    var params = parseQuery(query)
    params must contain key ('flag)
    params('flag) must equal ("true")
    unparseQuery(params) must equal (queryParams)
  }

  "Support query string with extra '?' in param name" in {
    val queryParams = "flag=true&" + encode("path?", encoding) + "=foo"
    val query = URI.create(baseURI + queryParams).getRawQuery
    var params = parseQuery(query)
    params must contain key ('flag)
    params('flag) must equal ("true")
    params must contain key (Symbol("path?"))
    params(Symbol("path?")) must equal ("foo")
    unparseQuery(params) must equal (queryParams)
  }

  "Support query string with random '?'" in {
    val queryParams = "flag=true&" + encode("path??path2", encoding)
    val query = URI.create(baseURI + queryParams).getRawQuery
    var params = parseQuery(query)
    params must contain key ('flag)
    params('flag) must equal ("true")
    params must contain key (Symbol("path??path2"))
    params(Symbol("path??path2")) must equal ("")
    unparseQuery(params) must equal (queryParams)
  }

  "Support empty parameter block '&&'" in {
    val queryParams = "flag=true&&foo=bar"
    val query = URI.create(baseURI + queryParams).getRawQuery
    var params = parseQuery(query)
    params must contain key ('flag)
    params('flag) must equal ("true")
    params must contain key ('foo)
    params('foo) must equal ("bar")
    unparseQuery(params) must equal (queryParams.replace("&&", "&"))
  }

  "Support empty URI as param value" in {
    val queryParams = "site=" + encode("http://www.google.com?search=blah", encoding)
    val query = URI.create(baseURI + queryParams).getRawQuery
    var params = parseQuery(query)
    params must contain key ('site)
    params('site) must equal ("http://www.google.com?search=blah")
    unparseQuery(params) must equal (queryParams)
  }
}
