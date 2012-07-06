/*
 * Copyright 2010 John A. De Goes
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
import org.scalatest.prop.Checkers
import org.scalacheck.{Gen, Arbitrary, Prop}
import Prop.forAll
import Arbitrary._

import JsonAST._

class JPathSpec extends WordSpec with MustMatchers with Checkers with ArbitraryJPath with ArbitraryJValue {
  "Parser" should {
    "parse all valid JPath strings" in {
      check { (jpath: JPath) =>
        JPath(jpath.toString) == jpath
      }
    }

    "forgivingly parse initial field name without leading dot" in {
      JPath("foo.bar").nodes must equal (JPathField("foo") :: JPathField("bar") :: Nil)
    }
  }

  "Extractor" should {
    "extract all existing paths" in {

      implicit val arb: Arbitrary[(JValue, List[(JPath, JValue)])] = Arbitrary {
        for (jv <- arbitrary[JObject]) yield (jv, jv.flattenWithPath)
      }

      check { (testData: (JValue, List[(JPath, JValue)])) =>
        testData match {
          case (obj, allPathValues) => 
            val allProps = allPathValues.map {
              case (path, pathValue) => path.extract(obj) == pathValue
            }
            allProps.foldLeft[Prop](true)(_ && _)
        }
      }
    }

    "extract a second level node" in {
      val j = JObject(JField("address", JObject( JField("city", JString("B")) :: JField("street", JString("2")) ::  Nil)) :: Nil)

      JPath("address.city").extract(j) must equal (JString("B"))
    }
  }

  "Parent" should {
    "return parent" in {
      JPath(".foo.bar").parent must be (Some(JPath(".foo")))
    }

    "return Identity for path 1 level deep" in {
      JPath(".foo").parent must be (Some(JPath.Identity))
    }

    "return None when there is no parent" in {
      JPath.Identity.parent must equal (None)
    }
  }

  "Ancestors" should {
    "return two ancestors" in {
      JPath(".foo.bar.baz").ancestors must equal (List(JPath(".foo.bar"), JPath(".foo"), JPath.Identity))
    }

    "return empty list for identity" in {
      JPath.Identity.ancestors must equal (Nil)
    }
  }

  "dropPrefix" should {
    "return just the remainder" in {
      JPath(".foo.bar[1].baz").dropPrefix(".foo.bar") must be (Some(JPath("[1].baz")))
    }

    "return none on path mismatch" in {
      JPath(".foo.bar[1].baz").dropPrefix(".foo.bar[2]") must be (None)
    }
  }

  "Ordering" should {
    "sort according to nodes names/indexes" in {
      val test = List(
        JPath("[1]"),
        JPath("[0]"),
        JPath("a"),
        JPath("a[9]"),
        JPath("a[10]"),
        JPath("b[10]"),
        JPath("a[10].a[1]"),
        JPath("b[10].a[1]"),
        JPath("b[10].a.x"),
        JPath("b[10].a[0]"),
        JPath("b[10].a[0].a")
      )

      val expected = List(1,0,2,3,4,6,5,9,10,7,8) map test

      test.sorted must equal (expected)
    }
  }
}
