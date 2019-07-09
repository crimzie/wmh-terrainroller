package com.crimzie.wmh

import doobie.implicits._
import doobie.util.transactor.Transactor
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
    def zioServerLogic(logic: I => IO[E, O]):
    ServerEndpoint[I, E, O, EntityBody[Task], Task] =
      ServerEndpoint(e, logic(_).either)
  }

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] = (for {
    pghost <- ZIO effect { sys.env("PGHOST") }
    pgpass <- ZIO effect { sys.env("PGPASS") }
    tx = Transactor.fromDriverManager[Task](
        "org.postgresql.Driver",
        s"jdbc:postgresql://$pghost/postgres",
        "postgres",
        pgpass)
    _ <- dao.PostgresChicken.testConnection().transact(tx)
    terrain <- ctrl.Terrain()
    chicken = new ctrl.Chicken(tx, dao.PostgresChicken, terrain)
    routes = List(
      api.Pages.index.zioServerLogic { _ => UIO succeed ctrl.Pages.indexPage },
      api.Pages.newChickenPageEndp.zioServerLogic { _ => UIO succeed ctrl.Pages.newChickenPage },
      api.Terrain.genTerrainEndp.zioServerLogic { terrain.setupTable },
      api.Terrain.terrainByIdEndp.zioServerLogic(chicken.readSetup),
      api.Chicken.newChickenEndp.zioServerLogic(chicken.createChicken),
      api.Chicken.completeChickenEndp.zioServerLogic { case (id, p) =>
        chicken.completeChicken(id, p)
      },
      api.Chicken.getChickenEndp.zioServerLogic(chicken.readChicken),
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
