package com.crimzie.wmh.controllers

import cats.effect._
import cats.implicits._
import cats.Applicative
import com.crimzie.wmh.daos.ChickenDao
import com.crimzie.wmh.model.Player
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

import scala.language.higherKinds
import scala.util.Random

class ChickenController[F[_] : Applicative](xa: Transactor[F], chickenDao: ChickenDao[ConnectionIO])
  (implicit val cs: ContextShift[F], br: Bracket[F, Throwable]) {

  def createChicken(p: Player): F[String] = {
    val id = Random.alphanumeric.take(16).mkString.toLowerCase
    chickenDao.add(id, p).transact(xa) map { _ => Pages.chickenUrlPage(id) }
  }

  def completeChicken(id: String, p: Player): F[String] =
    (for {
      _ <- chickenDao.complete(id, p, TerrainController.setupTable())
      c <- chickenDao.readChicken(id)
    } yield c)
      .transact(xa)
      .map { cd => Pages.chickenPage(id, cd.state.chicken.get) }

  def readChicken(id: String): F[String] =
    chickenDao.readChicken(id).transact(xa) map { cd =>
      cd.playerB.fold(Pages.challengePage(cd.playerA.name, id)) {
        _ => Pages.chickenPage(id, cd.state.chicken.get)
      }
    }

  def readSetup(id: String): F[Array[Byte]] = chickenDao.readSetup(id).transact(xa)
}
