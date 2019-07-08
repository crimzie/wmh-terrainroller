package com.crimzie.wmh.controllers

import cats.effect.Bracket
import com.crimzie.wmh.daos.ChickenDao
import com.crimzie.wmh.model.Player
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import tapir.model.StatusCode
import zio.{IO, Task, ZIO}

import scala.util.Random

class ChickenController(
    xa: Transactor[Task],
    chickenDao: ChickenDao[ConnectionIO],
    terrain: TerrainController,
)(implicit br: Bracket[Task, Throwable]) {

  def createChicken(p: Player): IO[Nothing, String] = {
    val id = Random.alphanumeric.take(8).mkString.toLowerCase
    chickenDao.add(id, p).transact(xa).orDie map { _ => Pages.chickenUrlPage(id) }
  }

  def completeChicken(id: String, p: Player): IO[StatusCode, String] =
    (for {
      table <- ZIO effectTotal { terrain.setupTable() }
      _ <- chickenDao.complete(id, p, table).transact(xa)
      cd <- chickenDao.readChicken(id).transact(xa)
    } yield cd).orDie.collect(404) { case Some(d) => Pages.chickenPage(id, d.state.chicken.get) }

  def readChicken(id: String): IO[StatusCode, String] =
    chickenDao.readChicken(id).transact(xa).orDie.collect(404) { case Some(cd) =>
      cd.playerB.fold(Pages.challengePage(cd.playerA.name, id)) {
        _ => Pages.chickenPage(id, cd.state.chicken.get)
      }
    }

  def readSetup(id: String): IO[StatusCode, Array[Byte]] =
    chickenDao.readSetup(id).transact(xa).orDie.collect(404) { case Some(a) => a }
}
