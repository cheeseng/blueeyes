/*
 * Copyright 2009-2010 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package blueeyes.json

//import _root_.org.scalacheck._
import _root_.org.scalacheck.Prop._
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.prop.Checkers
import org.scalacheck.Gen
import java.net.URLDecoder
import JsonAST._
import scala.util.control.Exception._

class JsonParserSpec extends WordSpec with MustMatchers with ArbitraryJValue with Checkers {
  import JsonAST._
  import JsonParser._
  import Printer._

  "Any valid json can be parsed" in {
    val parsing = (json: JValue) => { parse(Printer.pretty(render(json))); true }
    check(parsing)
  }

  "Buffer size does not change parsing result" in {
    val bufSize = Gen.choose(2, 64)
    val parsing = (x: JValue, s1: Int, s2: Int) => { parseVal(x, s1) == (parseVal(x, s2)) }
    forAll { (genObject: JValue, bufSize1: Int, bufSize2: Int) => 
      parsing(genObject, bufSize1, bufSize2)
    }
  }

  "Parsing is thread safe" in {
    import java.util.concurrent._

    val json = Examples.person
    val executor = Executors.newFixedThreadPool(100)
    val results = (0 to 100).map(_ =>
      executor.submit(new Callable[JValue] { def call = parse(json) })).toList.map(_.get)
    results.zip(results.tail).forall(pair => pair._1 == pair._2) must equal (true)
  }

  "All valid string escape characters can be parsed" in {
    parse("[\"abc\\\"\\\\\\/\\b\\f\\n\\r\\t\\u00a0\\uffff\"]") must equal (JArray(JString("abc\"\\/\b\f\n\r\t\u00a0\uffff")::Nil))
  }

  private def parseVal(json: JValue, bufSize: Int) = {
    val existingSize = JsonParser.Segments.segmentSize
    try {
      JsonParser.Segments.segmentSize = bufSize
      JsonParser.Segments.clear
      JsonParser.parse(compact(render(json)))
    } finally {
      JsonParser.Segments.segmentSize = existingSize
    }
  }
}

class ParserBugs extends WordSpec with MustMatchers {
  "Unicode ffff is a valid char in string literal" in {
    JsonParser.parseOpt(""" {"x":"\uffff"} """).isDefined must equal (true)
  }

  "Does not hang when parsing 2.2250738585072012e-308" in {
    allCatch.opt(JsonParser.parse(""" [ 2.2250738585072012e-308 ] """)) must equal (None)
  }

  "Does not hang when parsing 22.250738585072012e-309" in {
    allCatch.opt(JsonParser.parse(""" [ 22.250738585072012e-309 ] """)) must equal (None)
  }

  "Can parse funky characters" in {
    JsonParser.parse(URLDecoder.decode("\"%E2%84%A2\"", "UTF-8")) must equal (JString("™"))
  }
}
