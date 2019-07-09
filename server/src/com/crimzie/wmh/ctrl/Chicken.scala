package com.crimzie.wmh
package ctrl

import cats.effect.Bracket
import com.crimzie.wmh.model.Player
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import tapir.model.StatusCode
import zio.{IO, Task}

import scala.util.Random

class Chicken(
    xa: Transactor[Task],
    chickenDao: dao.Chicken[ConnectionIO],
    terrain: Terrain,
)(implicit br: Bracket[Task, Throwable]) {

  def createChicken(p: Player): IO[Nothing, String] = {
    val id = Random.alphanumeric.take(8).mkString.toLowerCase
    chickenDao.add(id, p).transact(xa).orDie map { _ => Pages.chickenUrlPage(id) }
  }

  def completeChicken(id: String, p: Player): IO[StatusCode, String] =
    (for {
      table <- terrain.setupTable(None)
      cd <- (for {
        _ <- chickenDao.complete(id, p, table).transact(xa)
        c <- chickenDao.readChicken(id).transact(xa)
      } yield c).orDie
    } yield cd).collect(404) { case Some(d) => Pages.chickenPage(id, d.state.chicken.get) }

  def readChicken(id: String): IO[StatusCode, String] =
    chickenDao.readChicken(id).transact(xa).orDie.collect(404) { case Some(cd) =>
      cd.playerB.fold(Pages.challengePage(cd.playerA.name, id)) {
        _ => Pages.chickenPage(id, cd.state.chicken.get)
      }
    }

  def readSetup(id: String): IO[StatusCode, Array[Byte]] =
    chickenDao.readSetup(id).transact(xa).orDie.collect(404) { case Some(a) => a }
}
