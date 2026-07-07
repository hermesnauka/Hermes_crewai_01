ThisBuild / version      := "0.1.0-phase1"
ThisBuild / scalaVersion := "3.3.8"

lazy val root = (project in file("."))
  .settings(
    name := "scalashield-backend",
    libraryDependencies ++= Seq(
      "dev.zio"        %% "zio"                  % "2.1.26",
      "dev.zio"        %% "zio-http"              % "3.11.3",
      "dev.zio"        %% "zio-json"              % "0.7.3",
      "io.getquill"    %% "quill-jdbc-zio"        % "4.8.6",
      "org.postgresql" %  "postgresql"            % "42.7.13",
      "com.zaxxer"     %  "HikariCP"              % "7.1.0",
      "org.flywaydb"   %  "flyway-core"           % "12.10.0",
      "org.flywaydb"   %  "flyway-database-postgresql" % "12.10.0",
      "ch.qos.logback" %  "logback-classic"       % "1.5.12",
      "dev.zio"        %% "zio-test"              % "2.1.26" % Test,
      "dev.zio"        %% "zio-test-sbt"          % "2.1.26" % Test,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  )
