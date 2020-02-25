import mill._
import mill.api.Loose
import mill.define.{Command, Target}
import mill.scalajslib.ScalaJSModule
import mill.scalalib._
import mill.eval.Result

  // `mill mill.scalalib.Dependency/updates` to look up updated dep versions

trait BaseModule extends ScalaModule {
  override def scalaVersion = "2.12.10"
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

  val tapirVer = "0.11.9"
  val zioVer = "1.0.0-RC17"
  val zioCatsVer = "2.0.0.0-RC10"
  val http4sVer = "0.20.19"
  val doobieVer = "0.8.8"
  val circeVer = "0.13.0"
  val scalatagsVer = "0.8.6"
  val scrimageVer = "2.1.8"
}

trait BaseJsModule extends BaseModule with ScalaJSModule {
  override def scalaJSVersion: Target[String] = "0.6.32"
}

object shared extends BaseJsModule {
  override def ivyDeps: Target[Loose.Agg[Dep]] = super.ivyDeps() ++ Agg(
    ivy"com.softwaremill.tapir::tapir-core:$tapirVer",
    ivy"com.softwaremill.tapir::tapir-json-circe:$tapirVer",
    ivy"io.circe::circe-generic:$circeVer",
  )
}

object server extends BaseModule {
  override def moduleDeps: Seq[JavaModule] = shared :: Nil
  override def ivyDeps: Target[Loose.Agg[Dep]] = super.ivyDeps() ++ Agg(
    ivy"com.softwaremill.tapir::tapir-http4s-server:$tapirVer",
    ivy"dev.zio::zio:$zioVer",
    ivy"dev.zio::zio-interop-cats:$zioCatsVer",
    ivy"org.http4s::http4s-blaze-server:$http4sVer",
    ivy"org.tpolecat::doobie-core:$doobieVer",
    ivy"org.tpolecat::doobie-postgres:$doobieVer",
    ivy"com.lihaoyi::scalatags:$scalatagsVer",
    ivy"com.sksamuel.scrimage::scrimage-core:$scrimageVer",
  )
  override def mainClass: Target[Some[String]] = Some("com.crimzie.wmh.Server")

  def version: Target[String] = "0.4.9"
  def tag: Target[String] =
    s"eu.gcr.io/wmh-terrain/wmh-terrainroller:${version()}"

  private def proc(p: os.Path, cmd: String*): Result[Unit] = 
    os.proc(cmd).call(p, stdout = os.Inherit, stderr = os.Inherit) match {
      case os.CommandResult(0, _) => Result.Success()
      case os.CommandResult(x, _) => Result.Failure(s"Exit code: $x")
    }

  def build(args: String*): Command[Unit] = T.command {
    assembly()
    val out = millSourcePath / "out"
    os.remove.all(out)
    os.makeDir.all(out)
    os.copy(assembly().path, out / "assembly.jar", replaceExisting = true)
    val t = tag()
    proc(millSourcePath, "docker", "build", "-t", t, ".")
  }

  def deploy(args: String*): Command[Unit] = T.command {
    build(args: _*)
    proc(millSourcePath, "docker", "push", tag())
  }
  // docker run -d -p80:8080 --tmpfs /tmp --restart on-failure eu.gcr.io/wmh-terrain/wmh-terrainroller:0.4.9
}
