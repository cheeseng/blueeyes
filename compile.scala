import scala.annotation.tailrec
import java.io._
import scala.collection.JavaConversions._

val ivy = System.getProperty("user.home") + "/.ivy2/cache"
val scalaz = ivy + "/org.scalaz/scalaz-core_2.9.1/jars/scalaz-core_2.9.1-7.0-SNAPSHOT.jar"
val specialScalaz = ivy + "/org.specs2/specs2-scalaz-core_2.9.1/jars/specs2-scalaz-core_2.9.1-6.0.1.jar"
val joda = ivy + "/joda-time/joda-time/jars/joda-time-1.6.2.jar"
val scalacheck = ivy + "/org.scala-tools.testing/scalacheck_2.9.1/jars/scalacheck_2.9.1-1.9.jar"
val conhashmp = ivy + "/com.googlecode.concurrentlinkedhashmap/concurrentlinkedhashmap-lru/jars/concurrentlinkedhashmap-lru-1.1.jar"
val codec = ivy + "/commons-codec/commons-codec/jars/commons-codec-1.5.jar"
val netty = ivy + "/org.jboss.netty/netty/bundles/netty-3.2.6.Final.jar"
val xlightweb = ivy + "/org.xlightweb/xlightweb/jars/xlightweb-2.13.2.jar"
val xsocket = ivy + "/org.xsocket/xSocket/jars/xSocket-2.8.15.jar"
val javolution = ivy + "/javolution/javolution/bundles/javolution-5.5.1.jar"
val akka = ivy + "/com.typesafe.akka/akka-actor/jars/akka-actor-2.0.2.jar"
val streum = ivy + "/org.streum/configrity_2.9.1/jars/configrity_2.9.1-0.9.0.jar"
val slf4j = ivy + "/org.slf4j/slf4j-api/jars/slf4j-api-1.6.1.jar"
val slf4s = ivy + "/com.weiglewilczek.slf4s/slf4s_2.9.1/jars/slf4s_2.9.1-1.0.7.jar"
val specs2 = ivy + "/org.specs2/specs2_2.9.1/jars/specs2_2.9.1-1.8.jar"
val mockito = ivy + "/org.mockito/mockito-all/jars/mockito-all-1.9.0.jar"
val testing = ivy + "/org.scala-tools.testing/scalacheck_2.9.1/jars/scalacheck_2.9.1-1.9.jar"
val servlet = ivy + "/javax.servlet/javax.servlet-api/jars/javax.servlet-api-3.0.1.jar"
val jettyServer = ivy + "/org.eclipse.jetty/jetty-server/jars/jetty-server-8.1.3.v20120416.jar"
val jettyServlet = ivy + "/org.eclipse.jetty/jetty-servlet/jars/jetty-servlet-8.1.3.v20120416.jar"
val jettySecurity = ivy + "/org.eclipse.jetty/jetty-security/jars/jetty-security-8.1.3.v20120416.jar"
val jettyUtil = ivy + "/org.eclipse.jetty/jetty-util/jars/jetty-util-8.1.3.v20120416.jar"
val jettyHttp = ivy + "/org.eclipse.jetty/jetty-http/jars/jetty-http-8.1.3.v20120416.jar"
val jettyIO = ivy + "/org.eclipse.jetty/jetty-io/jars/jetty-io-8.1.3.v20120416.jar"
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
    val newAccList = accList ++ files.filter(_.getName.endsWith(".scala")).map { f => f.getAbsolutePath }
    if (subDirs.isEmpty) 
      newAccList
    else 
      getScalaSourceFilesAcc(subDirs.flatMap(d => d.listFiles), newAccList)
  }
  getScalaSourceFilesAcc(srcDir.listFiles, List.empty)
}

val mainClasspathList = List(scalaz, 
                            specialScalaz, 
                            joda, 
                            scalacheck, 
                            conhashmp, 
                            codec, 
                            netty,
                            xlightweb,
                            xsocket,
                            javolution,
                            akka,
                            streum, 
                            slf4j,
                            slf4s, 
                            testing,
                            servlet, 
                            specs2, 
                            scalatest, 
                            ":target/classes")  // needs to be here so that when compiling core, it can find compiled json classes

val testClasspathList = mainClasspathList ++ 
                        List(mockito,
                             jettyServer,
                             jettyServlet, 
                             jettySecurity, 
                             jettyUtil, 
                             jettyHttp, 
                             jettyIO)

val mainClasspath = mainClasspathList.mkString(":")
val testClasspath = testClasspathList.mkString(":")

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
compile("core/main", "core/src/main/scala", mainClasspath, targetClasses.getAbsolutePath)
compile("core/test", "core/src/test/scala", testClasspath, targetTestClasses.getAbsolutePath)
val end = System.currentTimeMillis
copy("json/src/test/resources/diff-example-expected-additions.json", targetTestClasses.getAbsolutePath)
copy("json/src/test/resources/diff-example-expected-changes.json", targetTestClasses.getAbsolutePath)
copy("json/src/test/resources/diff-example-expected-deletions.json", targetTestClasses.getAbsolutePath)
copy("json/src/test/resources/diff-example-json1.json", targetTestClasses.getAbsolutePath)
copy("json/src/test/resources/diff-example-json2.json", targetTestClasses.getAbsolutePath)

println("Total compilation time: " + (end - start) + " millis.")
