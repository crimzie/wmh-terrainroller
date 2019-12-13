import ammonite.ops._
import mill._
import mill.api.Loose
import mill.define.{Command, Target}
import mill.scalajslib.ScalaJSModule
import mill.scalalib._
import mill.eval.Result

  // `mill mill.scalalib.Dependency/updates` to look up updated dep versions
  // `mill.scalalib.GenIdea/idea` to gen Idea project

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

  val tapirVer = "0.11.9"
  val zioVer = "1.0.0-RC17"
  val zioCatsVer = "2.0.0.0-RC10"
  val http4sVer = "0.20.15"
  val doobieVer = "0.8.6"
  val circeVer = "0.12.3"
  val scalatagsVer = "0.7.0"
  val scrimageVer = "2.1.8"
}

trait BaseJsModule extends BaseModule with ScalaJSModule {
  override def scalaJSVersion: Target[String] = "0.6.26"
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

  def version: Target[String] = "0.4.6"
  def tag: Target[String] =
    s"eu.gcr.io/wmh-terrain/wmh-terrainroller:${version()}"
  
  private val arrPrnt: (Array[Byte], Int) => Unit = { case (a, _) =>
      print(a.map(_.toChar).mkString)
      new Array[Byte](8192) copyToArray a
  }

  def build(args: String*): Command[Unit] = T.command {
    assembly()
    val out = millSourcePath / "out"
    rm(out)
    mkdir(out)
    cp(assembly().path, out / "assembly.jar")
    val t = tag()
    os
      .proc("docker", "build", "-t", t, ".")
      .stream(millSourcePath, onOut = arrPrnt, onErr = arrPrnt) match {
      case 0 =>
        println(s"docker run -d -p80:8080 --tmpfs /tmp --restart on-failure eu.gcr.io/wmh-terrain/wmh-terrainroller:$t")
        Result.Success {}
      case x => Result.Failure(x.toString)
    }
  }

  def deploy(args: String*): Command[Unit] = T.command {
    build(args: _*)
    os
      .proc("docker", "push", tag())
      .stream(millSourcePath, onOut = arrPrnt, onErr = arrPrnt) match {
      case 0 => Result.Success {}
      case x => Result.Failure(x.toString)
    }
  }
}
