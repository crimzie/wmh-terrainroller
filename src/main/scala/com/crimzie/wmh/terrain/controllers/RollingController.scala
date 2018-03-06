package com.crimzie.wmh.terrain.controllers

import monix.eval.Task
import org.http4s.Response
import org.http4s.dsl.io._

import scala.util.Random

object RollingController {

  private val losBlocks: Seq[String] = Seq(
    "Obstruction (Small base)",
    "Obstruction (Small base)",
    "Obstruction (Medium base)",
    "Obstruction (Medium base)",
    "Obstruction (Large base)",
    "Obstruction (Large base)",
    "Obstruction (Large base)",
    "Obstruction (Large base)",
    "Forest",
    "Forest",
    "Forest",
    "Forest",
    "Forest",
    "Forest",
    "Dense fog (3\")",
    "Dense fog (3\")",
    "Dense fog (4\")",
    "Dense fog (4\")",
    "Dense fog (5\")",
    "Dense fog (5\")",
  )

  private val losBlocksMap: Map[Int, String] =
    losBlocks.zipWithIndex.map(_.swap).toMap

  private val terrains: Seq[String] = Seq(
    "Obstacle (Cover)",
    "Obstacle (Cover)",
    "Obstacle (Cover)",
    "Obstacle (Concealment)",
    "Obstacle (Concealment)",
    "Obstacle (Concealment)",
    "Rubble",
    "Rubble",
    "Rubble",
    "Rubble",
    "Rubble",
    "Rubble",
    "Hill",
    "Hill",
    "Hill",
    "Tall Hill",
    "Tall Hill",
    "Tall Hill",
    "Shallow Water",
    "Shallow Water",
    "Shallow Water",
    "Shallow Water",
    "Shallow Water",
    "Shallow Water",
    "Trenches",
    "Trenches",
    "Trenches",
    "Trenches",
    "Trenches",
    "Trenches",
    "Rough Terrain",
    "Rough Terrain",
    "Rough Terrain",
    "Rough Terrain",
    "Rough Terrain",
    "Rough Terrain",
  )

  private val terrainsMap: Map[Int, String] =
    (losBlocks ++ terrains).zipWithIndex.map(_.swap).toMap

  private def l: String = losBlocksMap(Random nextInt losBlocksMap.size)

  private def t: String = terrainsMap(Random nextInt terrainsMap.size)

  private def ts(in: Seq[String], n: Int): Seq[String] =
    if (in.lengthCompare(n) == 0) in
    else {
      val r = t
      if (in.count(r.split(' ').head == _.split(' ').head) < 2) ts(in :+ r, n)
      else ts(in, n)
    }

  private val cluster: Task[String] = Task {
    val s = ts(Seq(l), 8)
    s"""Using randomly selected Cluster steamroller terrain setup method:
       |Place ${s.head} touching the center point of the table.
       |Place ${s(1)} within 6" of the center of the table, at ${Random.nextInt(12) + 1} o'clock.
       |Place ${s(2)}, ${s(3)} and ${s(4)} each within 6" of the center of the table and of preceding terrain piece,${if (Random.nextBoolean) "counter" else ""}clockwise.
       |Place ${s(5)}, ${s(6)} and ${s(7)} within 6" of each other and not within 8" of the center terrain cluster.""".stripMargin
  }

  private val quadrant: Task[String] = Task {
    val s = ts(Nil, 8)
    s"""Using randomly selected Quadrant steamroller terrain setup method:
       |Divide the table into four 24" x 24" quadrants.
       |Place ${s.head} within 5" of the center of the table and completely within top right quadrant.
       |Place ${s(1)} within 5" of the center of the table and completely within bottom right quadrant.
       |Place ${s(2)} within 5" of the center of the table and completely within bottom left quadrant.
       |Place ${s(3)} within 5" of the center of the table and completely within top left quadrant.
       |Place ${s(4)} within 5" of the center of top left quadrant.
       |Place ${s(5)} within 5" of the center of bottom left quadrant.
       |Place ${s(6)} within 5" of the center of bottom right quadrant.
       |Place ${s(7)} within 5" of the center of top right quadrant.""".stripMargin
  }

  private val scatter: Task[String] = Task {
    val s = ts(Seq(l), 8)
    val i = Random.nextInt(3)

    def d = 1 + Random.nextInt(3)

    def dd = 1 + Random.nextInt(12)

    s"""Using randomly selected Scatter steamroller terrain setup method:
       |Divide the table into four 24" x 24" quadrants.
       |Place ${s.head} touching the center point of the table.
       |Place ${s(1)} within $d" of top left quadrant center, at $dd o'clock.
       |Place ${s(2)} within $d" of bottom left quadrant center, at $dd o'clock.
       |Place ${s(3)} within $d" of bottom right quadrant center, at $dd o'clock.
       |Place ${s(4)} within $d" of top right quadrant center, at $dd o'clock.
       ${
      if (Random.nextBoolean)
        s"""|Place ${s(5)} within 10" of center of the table, at ${2 + d} o'clock.
            |Place ${s(6)} within 10" of center of the table, at ${8 + d} o'clock."""
      else
        s"""|Place ${s(5)} within 10" of center of the table, at ${8 + d} o'clock.
            |Place ${s(6)} within 10" of center of the table, at ${5 + d} o'clock.
            |Place ${s(7)} within 10" of center of the table, at ${(10 + d) % 12 + 1} o'clock."""
    }""".stripMargin
  }

  private val clock: Task[String] = Task {
    val s = ts(Seq(l), 7)
    val r = Random.nextInt(2)

    def d = 1 + Random.nextInt(3)

    s"Using Clock rolling method:\n" +
      s"Place ${s.head} touching the center.\n" +
      (for {i <- 1 to 6} yield
        s"Place ${s(d)} at ${d * 2 - r} o'clock, " +
          s"${10 + Random.nextInt(5)}${'"'} of center.\n").mkString
  }

  def randomMethod(): Task[Response[Task]] =
    cluster :: quadrant :: scatter :: Nil apply Random.nextInt(3) flatMap
      Response[Task](Ok).withBody[String]
}
