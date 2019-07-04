package com.crimzie.wmh

import cats.effect._
import cats.implicits._
import com.crimzie.wmh.controllers._
import com.crimzie.wmh.daos.PostgresChickenDao
import com.crimzie.wmh.model.Player
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

object Server extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = for {
    pghost <- IO { sys.env("PGHOST") }
    pgpass <- IO { sys.env("PGPASS") }
    tx <- IO {
      Transactor.fromDriverManager[IO](
        "org.postgresql.Driver",
        s"jdbc:postgresql://$pghost/postgres",
        "postgres",
        pgpass)
    }
    _ <- PostgresChickenDao.testConnection().transact(tx)
    chicken <- IO { new ChickenController[IO](tx, PostgresChickenDao) }
    html = headers.`Content-Type`(MediaType.text.html)
    png = headers.`Content-Type`(MediaType.image.png)
    routes = HttpRoutes.of[IO] {
      case GET -> Root =>
        Ok(Pages.indexPage, html)
      case GET -> Root / "chiken" =>
        Ok(Pages.newChickenPage, html)
      case GET -> Root / "terrain" =>
        IO { TerrainController.setupTable() } flatMap { Ok(_, png) }
      case GET -> Root / "terrain" / id =>
        chicken.readSetup(id) flatMap { Ok(_, png) }
      case req@POST -> Root / "chicken" / "new" =>
        req.as[Player] flatMap chicken.createChicken flatMap { Ok(_, html) }
      case req@POST -> Root / "chicken" / id =>
        req.as[Player] flatMap { chicken.completeChicken(id, _) } flatMap { Ok(_, html) }
      case GET -> Root / "chicken" / id =>
        chicken.readChicken(id) flatMap { Ok(_, html) }
    }.orNotFound
    _ <- IO { println("Starting server on port 8080.") }
    ec <- BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(routes)
      .serve.compile.drain
      .as(ExitCode.Success)
  } yield ec
}
