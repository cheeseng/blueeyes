package blueeyes.util

import org.scalatest._

class CommandLineArgumentsSpec extends WordSpec with MustMatchers {
  "No parameters or values should be parsed properly" in {
    val c = CommandLineArguments()
    
    c.parameters.size must equal (0)
    c.values.length must equal (0)
  }
  
  "Several parameters should be parsed properly" in {
    val c = CommandLineArguments("--foo", "bar", "--bar", "baz")
    
    c.parameters must equal (Map(
      "foo" -> "bar",
      "bar" -> "baz"
    ))
  }
  
  "Values combined with parameters should be parsed properly" in {
    val c = CommandLineArguments("baz", "--foo", "bar", "bar")
    
    c.parameters must equal (Map("foo" -> "bar"))
    c.values must equal (List("baz", "bar"))
  }
  
  "Parameters without values should have empty value strings" in {
    val c = CommandLineArguments("--baz", "--foo")
    
    c.parameters must equal (Map("baz" -> "", "foo" -> ""))
  }
  
  "Values combined with parameters should be counted properly" in {
    val c = CommandLineArguments("baz", "--foo", "bar", "bar")
    
    c.size must equal (3)
  }
}
