import scala.annotation.tailrec
import java.io._
import scala.collection.JavaConversions._

val ivy = System.getProperty("user.home") + "/.ivy2/cache"
val scalaz = ivy + "/org.scalaz/scalaz-core_2.9.1/jars/scalaz-core_2.9.1-7.0-SNAPSHOT.jar"
val joda = ivy + "/joda-time/joda-time/jars/joda-time-1.6.2.jar"
val scalacheck = ivy + "/org.scala-tools.testing/scalacheck_2.9.1/jars/scalacheck_2.9.1-1.9.jar"
val scalatest = "lib/scalatest.jar"

val targetClasses = new File("target/classes")
targetClasses.delete()
targetClasses.mkdir()

val targetTestClasses = new File("target/test_classes")
targetTestClasses.delete()
targetTestClasses.mkdir()

def getScalaSourceFiles(srcDir: File) = {
  @tailrec
  def getScalaSourceFilesAcc(dirList: Array[File], accList: List[String]): List[String] = {
    val (files, subDirs) = dirList.partition(_.isFile)
    val newAccList = accList ++ files.map { f => f.getAbsolutePath }
    if (subDirs.isEmpty) 
      newAccList
    else 
      getScalaSourceFilesAcc(subDirs.flatMap(d => d.listFiles), newAccList)
  }
  getScalaSourceFilesAcc(srcDir.listFiles, List.empty)
}

val mainClasspath = scalaz + ":" + joda + ":" + scalacheck
val testClasspath = scalaz + ":" + joda + ":" + scalacheck + ":" + scalatest + ":target/classes" 

def compile(name: String, srcDir: String, classpath: String, targetDir: String) {
  val sourceFiles = getScalaSourceFiles(new File(srcDir)).toList
  val command = List("scalac", "-classpath", classpath, "-d", targetDir) ++ sourceFiles
  val builder = new ProcessBuilder(command)
  builder.redirectErrorStream(true)
  val process = builder.start()

  val stdout = new BufferedReader(new InputStreamReader(process.getInputStream))

  var line = "Starting compilation of " + name + "..."
  while (line != null) {
    println (line)
    line = stdout.readLine
  }
}

def copy(srcFilePath: String, destDirPath: String) {
  val srcFile = new File(srcFilePath)
  val destDir = new File(destDirPath)
  val destFile = new File(destDir, srcFile.getName)
  new FileOutputStream(destFile) getChannel() transferFrom(
      new FileInputStream(srcFile) getChannel(), 0, Long.MaxValue)

}

val start = System.currentTimeMillis

compile("json/main", "json/src/main/scala", mainClasspath, targetClasses.getAbsolutePath)
compile("json/test", "json/src/test/scala", testClasspath, targetTestClasses.getAbsolutePath)
copy("json/src/test/resources/diff-example-expected-additions.json", targetTestClasses.getAbsolutePath)
copy("json/src/test/resources/diff-example-expected-changes.json", targetTestClasses.getAbsolutePath)
copy("json/src/test/resources/diff-example-expected-deletions.json", targetTestClasses.getAbsolutePath)
copy("json/src/test/resources/diff-example-json1.json", targetTestClasses.getAbsolutePath)
copy("json/src/test/resources/diff-example-json2.json", targetTestClasses.getAbsolutePath)

val end = System.currentTimeMillis
println("Total compilation time: " + (end - start) + " millis.")
