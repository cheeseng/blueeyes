package blueeyes.json.xschema.codegen {

  import _root_.blueeyes.json.JsonAST._
  import _root_.blueeyes.json.JsonParser._
  import java.io._
  import org.scalatest.WordSpec
  import org.scalatest.matchers.MustMatchers

class XCodeGeneratorExamples extends WordSpec with MustMatchers {
    import _root_.java.io._
    import _root_.blueeyes.json.xschema.SampleSchemas._
    import _root_.blueeyes.json.xschema.DefaultSerialization._
    import CodeGenerator._
    
    trait Uncloseable extends Closeable {
      override def close() = { }
    }
  
    "the xschema code generator" should {
      "generate the schema for XSchema without exceptions" in {
        val out = using(new StringWriter) {
          sw => using(new PrintWriter(sw)) { out => 
            ScalaCodeGenerator.generator.generate(XSchemaSchema, "src/main/scala", "src/test/scala", Nil, _ => out)
            sw.toString
          }
        }

        out must not equal ""
      }
    }
    
    "the xschema code generator" should {
      "generate the schema for FringeSchema without exceptions" in {
        val sw = new StringWriter()
        
        val out = using(new PrintWriter(sw) with Uncloseable) { out => 
          HaXeCodeGenerator.generator.generate(XSchemaSchema, "src/main/scala", "src/test/scala", Nil, _ => out)
          sw.toString
        }
        
        //println(sw)

        out must not equal ""
      }
    }
  }
}
