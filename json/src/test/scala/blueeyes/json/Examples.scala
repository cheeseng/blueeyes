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

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import JsonAST._
import JsonDSL._
import JsonParser._
import Examples._

class Examples extends WordSpec with MustMatchers {

  "Lotto example" in {
    val json = parse(lotto)
    val renderedLotto = compact(render(json))
    json must equal (parse(renderedLotto))
  }

  "Person example" in {
    val json = parse(person)
    val renderedPerson = JsonDSL.pretty(render(json))
    json must equal (parse(renderedPerson))
    render(json) must equal (render(personDSL))
    compact(render(json \\ "name")) must equal ("""["Joe","Marilyn"]""")
    compact(render(json \ "person" \ "name")) must equal ("\"Joe\"")
  }

  "Transformation example" in {
    val uppercased = parse(person).transform { case JField(n, v) => JField(n.toUpperCase, v) }
    val rendered = compact(render(uppercased))
    rendered must equal (
      """{"PERSON":{"NAME":"Joe","AGE":35,"SPOUSE":{"PERSON":{"NAME":"Marilyn","AGE":33}}}}""")
  }

  "Remove example" in {
    val json = parse(person) remove { _ == JField("name", "Marilyn") }
    compact(render(json \\ "name")) must equal ("\"Joe\"")
  }

  "Queries on person example" in {
    val json = parse(person)
    val filtered = json filter {
      case JField("name", _) => true
      case _ => false
    }
    filtered must equal (List(JField("name", JString("Joe")), JField("name", JString("Marilyn"))))

    val found = json find {
      case JField("name", _) => true
      case _ => false
    }
    found must equal (Some(JField("name", JString("Joe"))))
  }

  "Object array example" in {
    val json = parse(objArray)
    compact(render(json \ "children" \ "name")) must equal ("""["Mary","Mazy"]""")
    compact(render((json \ "children")(0) \ "name")) must equal ("\"Mary\"")
    compact(render((json \ "children")(1) \ "name")) must equal ("\"Mazy\"")
    (for { JField("name", JString(y)) <- json } yield y) must equal (List("joe", "Mary", "Mazy"))
  }

  "Unbox values using XPath-like type expression" in {
    parse(objArray) \ "children" \\ classOf[JInt] must equal (List(5, 3))
    parse(lotto) \ "lotto" \ "winning-numbers" \ classOf[JInt] must equal (List(2, 45, 34, 23, 7, 5, 3))
    parse(lotto) \\ "winning-numbers" \ classOf[JInt] must equal (List(2, 45, 34, 23, 7, 5, 3))
  }

  "Quoted example" in {
    val json = parse(quoted)
    List("foo \" \n \t \r bar") must equal (json.values)
  }

  "Null example" in {
    compact(render(parse(""" {"name": null} """))) must equal ("""{"name":null}""")
  }

  "Symbol example" in {
    compact(render(symbols)) must equal ("""{"f1":"foo","f2":"bar"}""")
  }

  "Unicode example" in {
    parse("[\" \\u00e4\\u00e4li\\u00f6t\"]") must equal (JArray(List(JString(" \u00e4\u00e4li\u00f6t"))))
  }

  "Exponent example" in {
    parse("""{"num": 2e5 }""") must equal (JObject(List(JField("num", JDouble(200000.0)))))
    parse("""{"num": -2E5 }""") must equal (JObject(List(JField("num", JDouble(-200000.0)))))
    parse("""{"num": 2.5e5 }""") must equal (JObject(List(JField("num", JDouble(250000.0)))))
    parse("""{"num": 2.5e-5 }""") must equal (JObject(List(JField("num", JDouble(2.5e-5)))))
  }

  "JSON building example" in {
    val json = concat(JField("name", JString("joe")), JField("age", JInt(34))) ++ concat(JField("name", JString("mazy")), JField("age", JInt(31)))
    compact(render(json)) must equal ("""[{"name":"joe","age":34},{"name":"mazy","age":31}]""")
  }

  "JSON building with implicit primitive conversions example" in {
    import Implicits._
    val json = concat(JField("name", "joe"), JField("age", 34)) ++ concat(JField("name", "mazy"), JField("age", 31))
    compact(render(json)) must equal ("""[{"name":"joe","age":34},{"name":"mazy","age":31}]""")
  }

  "Example which collects all integers and forms a new JSON" in {
    val json = parse(person)
    val ints = json.foldDown(JNothing: JValue) { (a, v) => v match {
      case x: JInt => a ++ x
      case _ => a
    }}
    compact(render(ints)) must equal ("""[35,33]""")
  }

  "Example which folds up to form a flattened list" in {
    val json = parse(person)

    def form(list: JPath*): List[(JPath, JValue)] = list.toList.map { path =>
      (path, json(path))
    }

    val folded = (json.foldUpWithPath[List[(JPath, JValue)]](Nil) { (list, path, json) =>
      (path, json) :: list
    }).reverse.collect { case (p, j) if (!j.isInstanceOf[JField]) => (p, j) }

    val formed = form(
      JPath("person.name"),
      JPath("person.age"),
      JPath("person.spouse.person.name"),
      JPath("person.spouse.person.age"),
      JPath("person.spouse.person"),
      JPath("person.spouse"),
      JPath("person"),
      JPath.Identity
    )

    folded must equal (formed)
  }

  "Renders JSON as Scala code" in {
    val json = parse(lotto)

    Printer.compact(renderScala(json)) must equal ("""JObject(JField("lotto",JObject(JField("lotto-id",JInt(5))::JField("winning-numbers",JArray(JInt(2)::JInt(45)::JInt(34)::JInt(23)::JInt(7)::JInt(5)::JInt(3)::Nil))::JField("winners",JArray(JObject(JField("winner-id",JInt(23))::JField("numbers",JArray(JInt(2)::JInt(45)::JInt(34)::JInt(23)::JInt(3)::JInt(5)::Nil))::Nil)::JObject(JField("winner-id",JInt(54))::JField("numbers",JArray(JInt(52)::JInt(3)::JInt(12)::JInt(11)::JInt(18)::JInt(22)::Nil))::Nil)::Nil))::Nil))::Nil)""")
  }
}

object Examples {
  val lotto = """
{
  "lotto":{
    "lotto-id":5,
    "winning-numbers":[2,45,34,23,7,5,3],
    "winners":[ {
      "winner-id":23,
      "numbers":[2,45,34,23,3, 5]
    },{
      "winner-id" : 54 ,
      "numbers":[ 52,3, 12,11,18,22 ]
    }]
  }
}
"""

  val person = """
{
  "person": {
    "name": "Joe",
    "age": 35,
    "spouse": {
      "person": {
        "name": "Marilyn",
        "age": 33
      }
    }
  }
}
"""

  val personDSL =
    ("person" ->
      ("name" -> "Joe") ~
      ("age" -> 35) ~
      ("spouse" ->
        ("person" ->
          ("name" -> "Marilyn") ~
          ("age" -> 33)
        )
      )
    )

  val objArray =
"""
{ "name": "joe",
  "address": {
    "street": "Bulevard",
    "city": "Helsinki"
  },
  "children": [
    {
      "name": "Mary",
      "age": 5
    },
    {
      "name": "Mazy",
      "age": 3
    }
  ]
}
"""

  val quoted = """["foo \" \n \t \r bar"]"""
  val symbols = ("f1" -> 'foo) ~ ("f2" -> 'bar)
}
