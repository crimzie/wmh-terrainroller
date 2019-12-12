package com.crimzie.wmh

import cats.implicits._
import com.crimzie.wmh.api.{FilesApi, PagesApi, TerrainApi}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.EntityBody
import tapir._
import tapir.server.http4s._
import tapir.server.ServerEndpoint
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

object Server extends App with CatsApp {

  implicit class ZioEndpoint[I, E, O](e: Endpoint[I, E, O, EntityBody[Task]]) {
    def zioServerLogic(logic: I => IO[E, O]): ServerEndpoint[I, E, O, EntityBody[Task], Task] =
      ServerEndpoint(e, logic(_).either)

    def pureServerLogic(logic: I => O): ServerEndpoint[I, E, O, EntityBody[Task], Task] =
      ServerEndpoint(e, in => UIO { logic(in).asRight })
  }

  override def run(args: List[String]): URIO[zio.ZEnv, Int] = (for {
    terrain <- ctrl.TerrainCtrl()
    files <- ctrl.Files()
    routes = List(
      PagesApi.index.pureServerLogic { _ => ctrl.Pages.indexPage },
      PagesApi.newChickenPageEndp.pureServerLogic { _ => ctrl.Pages.newChickenPage },
      FilesApi.headerImg.pureServerLogic { _ => files.headerImg },
      FilesApi.textureImg.pureServerLogic { _ => files.textureImg },
      FilesApi.textureFooImg.pureServerLogic { _ => files.textureFooImg },
      FilesApi.styleCss.pureServerLogic { _ => files.styleCss },
      TerrainApi.genRandTerrainEndp.zioServerLogic { tt => terrain.setupTable(None)(Some(tt)) },
      TerrainApi.genTerrainEndp.zioServerLogic { case (i, tt) =>
        terrain.setupTable(Some(i))(Some(tt))
      },
    ).toRoutes.orNotFound
    _ <- IO { println("Starting server on port 8080.") }
    _ <- BlazeServerBuilder[Task]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(routes)
      .serve.compile.drain
  } yield ()).fold(
    { t => t.printStackTrace(); 1 },
    _ => 0)
}
