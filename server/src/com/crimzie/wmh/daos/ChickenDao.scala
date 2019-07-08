package com.crimzie.wmh.daos

import com.crimzie.wmh.model._
import doobie._
import doobie.implicits._
import cats.implicits._

import scala.language.higherKinds

trait ChickenDao[F[_]] {
  def add(id: String, player: Player): F[Unit]

  def complete(id: String, player: Player, setup: Array[Byte]): F[Option[Unit]]

  def readChicken(id: String): F[Option[ChickenData]]

  def readSetup(id: String): F[Option[Array[Byte]]]
}

object PostgresChickenDao extends ChickenDao[ConnectionIO] {
  override def add(
      id: String,
      player: Player,
  ): ConnectionIO[Unit] =
    sql"""
      insert into Chicken (Id, PlayerA, ListA1, ListA2)
      values ($id, ${player.name}, ${player.list1}, ${player.list2});
      """.update.run.map { _ => () }

  override def complete(
      id: String,
      player: Player,
      setup: Array[Byte],
  ): ConnectionIO[Option[Unit]] =
    sql"""
      update Chicken
      set
        PlayerB = ${player.name},
        ListB1 = ${player.list1},
        ListB2 = ${player.list2},
        Setup = $setup
      where Id = $id;
      """.update.run map {
      case 0 => None
      case 1 => ().some
      case _ => throw new Exception("More than one DB entry updated with given Id.")
    }

  override def readChicken(id: String): ConnectionIO[Option[ChickenData]] =
    sql"""
      select PlayerA, ListA1, ListA2, PlayerB, ListB1, ListB2
      from Chicken
      where Id = $id;
      """.query[ChickenData].option

  override def readSetup(id: String): ConnectionIO[Option[Array[Byte]]] =
    sql"""
      select Setup from Chicken where id = $id;
      """.query[Array[Byte]].option

  def testConnection(): ConnectionIO[Int] =
    sql"select count(Id) from Chicken;".query[Int].unique
}
