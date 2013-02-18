name := "gu-id-store"

version := "1.0"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
    "com.google.inject.extensions" % "guice-servlet" % "3.0",
    "org.eclipse.jetty" % "jetty-webapp" % "7.6.8.v20121106" % "container",
    "javax.servlet" % "servlet-api" % "2.5" % "provided",
    "net.liftweb" %% "lift-json" % "2.5-M4",
    "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test"
)

appengineSettings
