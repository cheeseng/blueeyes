import java.io._
import scala.collection.JavaConversions._

val ivy = System.getProperty("user.home") + "/.ivy2/cache"
val scalaz = ivy + "/org.scalaz/scalaz-core_2.9.1/jars/scalaz-core_2.9.1-7.0-SNAPSHOT.jar"
val joda = ivy + "/joda-time/joda-time/jars/joda-time-1.6.2.jar"
val scalacheck = ivy + "/org.scala-tools.testing/scalacheck_2.9.1/jars/scalacheck_2.9.1-1.9.jar"
val scalatest = "lib/scalatest.jar"

val testClasspath = scalaz + ":" + joda + ":" + scalacheck + ":" + scalatest + ":target/classes"

val command = List("scala", "-cp", testClasspath, "org.scalatest.tools.Runner", "-p", "target/test_classes")
val builder = new ProcessBuilder(command)
builder.redirectErrorStream(true)
val process = builder.start()

val stdout = new BufferedReader(new InputStreamReader(process.getInputStream))

var line = "Starting to run tests..."
while (line != null) {
  println (line)
  line = stdout.readLine
}
