package blueeyes.json.xschema.codegen {

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

import java.io.{Writer, PrintWriter}

class XSchemaDatabaseExamples extends WordSpec with MustMatchers {
  import _root_.blueeyes.json.JsonAST._
  import _root_.blueeyes.json.JsonParser._
  import _root_.blueeyes.json.xschema._
  import _root_.blueeyes.json.xschema.DefaultSerialization._
  import _root_.blueeyes.json.xschema.SampleSchemas._
  
  "Common primitive fields in products of a coproduct are identified" in {
    val db = XSchemaDatabase(DataSocialGenderSchema)
    
    val coproduct = DataSocialGenderSchema.definitions.filter(_.isInstanceOf[XCoproduct]).map(_.asInstanceOf[XCoproduct]).head
    
    val commonFields = db.commonFieldsOf(coproduct)
    
    commonFields.length must equal (1)
    commonFields.head._1 must equal ("text")
    commonFields.head._2 must equal (XString)
  }
  
  "Common coproduct fields in products of a coproduct are identified" in {
    val db = XSchemaDatabase(EmployeeSchema)
    
    val employee = db.definitionByName("Employee").get.asInstanceOf[XCoproduct]
    
    val commonFields = db.commonFieldsOf(employee)
    
    commonFields.length must equal (1)
    commonFields.head._1 must equal ("id")
    commonFields.head._2 must equal (XDefinitionRef("SSN", "data.employee"))
  }
}

} 
