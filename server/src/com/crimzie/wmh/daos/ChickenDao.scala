package com.crimzie.wmh.daos

import com.crimzie.wmh.model._
import doobie._
import doobie.implicits._

import scala.language.higherKinds

trait ChickenDao[F[_]] {
  def add(id: String, player: Player): F[Unit]

  def complete(id: String, player: Player, setup: Array[Byte]): F[Unit]

  def readChicken(id: String): F[ChickenData]

  def readSetup(id: String): F[Array[Byte]]
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
  ): ConnectionIO[Unit] =
    sql"""
      update Chicken
      set
        PlayerB = ${player.name},
        ListB1 = ${player.list1},
        ListB2 = ${player.list2},
        Setup = $setup
      where Id = $id;
      """.update.run.map { _ => () }

  override def readChicken(id: String): ConnectionIO[ChickenData] =
    sql"""
      select PlayerA, ListA1, ListA2, PlayerB, ListB1, ListB2
      from Chicken
      where Id = $id;
      """.query[ChickenData].unique

  override def readSetup(id: String): ConnectionIO[Array[Byte]] =
    sql"""
      select Setup from Chicken where id = $id;
      """.query[Array[Byte]].unique

  def testConnection(): ConnectionIO[Int] =
    sql"select count(Id) from Chicken;".query[Int].unique
}
