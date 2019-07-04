import ammonite.ops._
import mill._
import mill.define.{Command, Target}
import mill.scalajslib.ScalaJSModule
import mill.scalalib._
import mill.eval.Result

trait BaseModule extends ScalaModule {
  override def scalaVersion = "2.12.8"
  override def scalacOptions: Target[Seq[String]] =
    super.scalacOptions() ++ Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:_",
      "-Yno-adapted-args",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Ywarn-unused-import",
      "-Ypartial-unification",
      "-Xmacro-settings:materialize-derivations",
    )

  val http4sVer = "0.20.3"
  val doobieVer = "0.7.0"
  val circeVer = "0.11.1"
  val scalatagsVer = "0.7.0"
  val scrimageVer = "2.1.8"
}

trait BaseJsModule extends BaseModule with ScalaJSModule {
  override def scalaJSVersion: Target[String] = "0.6.26"
}

object shared extends BaseJsModule {}

object server extends BaseModule {
  override def moduleDeps: Seq[JavaModule] = shared :: Nil
  override def ivyDeps = Agg(
    ivy"org.http4s::http4s-dsl:$http4sVer",
    ivy"org.http4s::http4s-blaze-server:$http4sVer",
    ivy"org.http4s::http4s-circe:$http4sVer",
    ivy"io.circe::circe-generic:$circeVer",
    ivy"org.tpolecat::doobie-core:$doobieVer",
    ivy"org.tpolecat::doobie-postgres:$doobieVer",
    ivy"com.lihaoyi::scalatags:$scalatagsVer",
    ivy"com.sksamuel.scrimage::scrimage-core:$scrimageVer",
  )
  override def mainClass: Target[Some[String]] = Some("com.crimzie.wmh.Server")

  def version: Target[String] = "0.3.9"
  def deploy(args: String*): Command[Unit] = T.command {
    assembly()
    val tag = s"eu.gcr.io/wmh-terrain/wmh-terrainroller:${version()}"
    val out = millSourcePath / "out"
    mkdir(out)
    rm(out / "assembly.jar")
    cp(assembly().path, out / "assembly.jar")
    %%(
      "docker",
      "build",
      "-t",
      tag,
      "--build-arg",
      s"pghost=${sys.env("PGHOST")}",
      "--build-arg",
      s"pgpass=${sys.env("PGPASS")}",
      ".")(millSourcePath)
      .chunks
      .foreach(_.fold(print, print))
    %%("docker", "push", tag)(millSourcePath)
      .chunks
      .foreach(_.fold(print, print))
    rm(out)
    Result.Success{}
  }
}
