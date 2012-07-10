name := "blueeyes-json"

publishArtifact in Test := true

libraryDependencies ++= Seq(
  "org.scalaz"                  %% "scalaz-core"        % "7.0-SNAPSHOT" changing(),
  "joda-time"                   %  "joda-time"          % "1.6.2"          % "optional",
  "org.scala-tools.testing"     %  "scalacheck_2.9.1"   % "1.9"            % "test"
)
