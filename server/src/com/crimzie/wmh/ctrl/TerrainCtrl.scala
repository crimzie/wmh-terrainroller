package com.crimzie.wmh
package ctrl

import cats.effect.Effect
import com.crimzie.wmh.model.Terrain
import com.sksamuel.scrimage._
import tapir.model.StatusCode
import zio.{IO, UIO}

import scala.util.Random

class TerrainCtrl private() {

  private val scenarios: Seq[String] = Seq(
    "/scenario1.png",
    "/scenario2.png",
    "/scenario3.png",
    "/scenario4.png",
    "/scenario5.png",
    "/scenario6.png",
  )

  private def toImage(t: model.Terrain): Image = Image.fromResource(t.rsc).scale(0.5)

  private val losBlocks: Seq[model.Terrain] =
    Seq(
      model.Terrain.LosBlock.Obstruction,
      model.Terrain.LosBlock.Obstruction,
      model.Terrain.LosBlock.Forest,
      model.Terrain.LosBlock.Forest,
      model.Terrain.LosBlock.Cloud,
      model.Terrain.LosBlock.Cloud,
    )

  private val terrains: Seq[model.Terrain] =
    losBlocks ++ Seq(
      model.Terrain.Other.Wall,
      model.Terrain.Other.Wall,
      model.Terrain.Other.Fence,
      model.Terrain.Other.Fence,
      model.Terrain.Other.Rubble,
      model.Terrain.Other.Rubble,
      model.Terrain.Other.Water,
      model.Terrain.Other.Water,
      model.Terrain.Other.Trench,
      model.Terrain.Other.Trench,
      model.Terrain.Other.Rough,
      model.Terrain.Other.Rough,
    )

  private val legend: Image = Image.fromResource("/legend.png")

  private def random[A](l: Seq[A]): A = l(Random.nextInt(l.size))

  private def shuffleUpLB(l: Seq[model.Terrain]): Seq[Image] = {
    val bi = l.indexWhere { _.isInstanceOf[model.Terrain.LosBlock] }
    val btt =
      if (bi > -1) l(bi) +: Random.shuffle(l.take(bi) ++ l.drop(bi + 1)) else Random.shuffle(l)
    btt map toImage
  }

  private val π: Double = math.Pi

  case class Coord(x: Double, y: Double)

  private def deviation(
      c: Coord,
      d: Int,
      a: Double,
  ): Coord = Coord(math.cos(a) * d + c.x, math.sin(a) * d + c.y)

  private def randomDev(c: Coord, max: Int): Coord = {
    val d = Random.nextInt(max + 1)
    val a = Random.nextDouble() % (π * 2)
    Coord(math.cos(a) * d + c.x, math.sin(a) * d + c.y)
  }

  private def render(sc: Image, s: Seq[(Image, Coord)]): Image =
    s.iterator
      .foldLeft(sc) {
        case (i, (t, c)) => i.overlay(t, c.x * 20 - 30 toInt, c.y * 20 - 30 toInt)
      }
      .resizeTo(960, 1360, Position.TopCenter, Color.White)
      .overlay(legend, y = 960)

  private def cluster(sc: Image, tt: Seq[model.Terrain]): Image = {
    val center = randomDev(Coord(24, 24), 2)
    val dir: Double = Random.nextInt(12) / 6.0 * π
    val xys = Seq(
      center,
      randomDev(deviation(center, 9, dir), 1),
      randomDev(deviation(center, 9, dir + π / 3.0), 1),
      randomDev(deviation(center, 9, dir + π / 1.5), 1),
      randomDev(deviation(center, 9, dir + π), 1),
      randomDev(deviation(center, 13, dir - π / 3.0), 1),
      randomDev(deviation(center, 13, dir - π / 2.0), 1),
      randomDev(deviation(center, 13, dir - π / 1.5), 1),
    )
    render(sc, shuffleUpLB(tt).zip(xys))
  }

  private def quadrant(sc: Image, tt: Seq[model.Terrain]): Image = {
    val xys = Seq(
      deviation(
        Coord(26, 22),
        Random.nextInt(5),
        Random.nextDouble() % (π / 2.0) + π * 1.5),
      deviation(
        Coord(26, 26),
        Random.nextInt(5),
        Random.nextDouble() % (π / 2.0)),
      deviation(
        Coord(22, 26),
        Random.nextInt(5),
        Random.nextDouble() % (π / 2.0) + π / 2.0),
      deviation(
        Coord(22, 22),
        Random.nextInt(5),
        Random.nextDouble() % (π / 2.0) + π),
      randomDev(Coord(12, 14), 3),
      randomDev(Coord(12, 34), 3),
      randomDev(Coord(36, 34), 3),
      randomDev(Coord(36, 14), 3),
    )
    render(sc, Random.shuffle(tt) map toImage zip xys)
  }

  private def scatter(sc: Image, tt: Seq[model.Terrain]): Image = {
    val xysBase = Seq(
      randomDev(Coord(12, 14), 3),
      randomDev(Coord(12, 34), 3),
      randomDev(Coord(36, 34), 3),
      randomDev(Coord(36, 14), 3),
    )
    val xysVar =
      if (Random.nextBoolean()) Seq(
        randomDev(Coord(24, 16), 2),
        randomDev(Coord(24, 32), 2),
        randomDev(Coord(if (Random.nextBoolean()) 17 else 31, 24), 2),
      ) else Seq(
        randomDev(Coord(17, 24), 3),
        randomDev(Coord(31, 24), 3),
      )
    val xys = randomDev(Coord(24, 24), 2) +: (xysBase ++ xysVar)
    render(sc, shuffleUpLB(tt).zip(xys))
  }

  /** Png image as byte array with default implicit writer. */
  def setupTable(n: Option[Int])(ott: Option[Seq[Terrain]]): IO[StatusCode, Array[Byte]] = UIO {
    Image.fromResource(n.fold(random(scenarios)) { n => scenarios(n - 1) }).scaleTo(960, 960)
  } catchAll { _ => IO.fail(400) } map { scenario =>
    val tt = ott getOrElse terrains
    (Random.nextInt(3) match {
      case 0 => cluster(scenario, tt)
      case 1 => quadrant(scenario, tt)
      case 2 => scatter(scenario, tt)
    }).bytes
  }
}

object TerrainCtrl {def apply[F[_] : Effect](): F[TerrainCtrl] = Effect[F].pure(new TerrainCtrl())}
