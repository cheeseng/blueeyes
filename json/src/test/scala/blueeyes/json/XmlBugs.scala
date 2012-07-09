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

package blueeyes {
package json {

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

class XmlBugs extends WordSpec with MustMatchers {
  import JsonAST._
  import Printer._
  import Xml._
  import scala.xml.{Group, Text}

  "HarryH's XML parses correctly" in {
    val xml1 = <venue><id>123</id></venue>
    val xml2 = <venue> <id>{"1"}{"23"}</id> </venue>
    Xml.toJson(xml1) must equal (Xml.toJson(xml2))
  }

  "HarryH's XML with attributes parses correctly" in {
    val json = toJson(<tips><group type="Nearby"><tip><id>10</id></tip></group></tips>)
    Printer.compact(render(json)) must equal ("""{"tips":{"group":{"type":"Nearby","tip":{"id":"10"}}}}""")
  }

  "Jono's XML with attributes parses correctly" in {
    val example1 = <word term="example" self="http://localhost:8080/word/example" available="true">content</word>
    val expected1 = """{"word":"content","available":"true","term":"example","self":"http://localhost:8080/word/example"}"""

    val example2 = <word term="example" self="http://localhost:8080/word/example" available="true"></word>
    val expected2 = """{"available":"true","term":"example","self":"http://localhost:8080/word/example"}"""

    Printer.compact(render(toJson(example1))) must equal (expected1)
    Printer.compact(render(toJson(example2))) must equal (expected2)
  }

  "Nodes with attributes converted to correct JSON" in {
    val xml =
      <root>
        <n id="10" x="abc" />
        <n id="11" x="bcd" />
      </root>
    val expected = """{"root":{"n":[{"x":"abc","id":"10"},{"x":"bcd","id":"11"}]}}"""
    Printer.compact(render(toJson(xml))) must equal (expected)
  }

  "XML with empty node is converted correctly to JSON" in {
    val xml =
      <tips><group type="Foo"></group><group type="Bar"><tip><text>xxx</text></tip><tip><text>yyy</text></tip></group></tips> 
    val expected = """{"tips":{"group":[{"type":"Foo"},{"type":"Bar","tip":[{"text":"xxx"},{"text":"yyy"}]}]}}"""
    Printer.compact(render(toJson(xml))) must equal (expected)
  }
}

}
}
