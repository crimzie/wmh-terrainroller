package com.crimzie.wmh
package ctrl

import cats.effect.Effect
import com.sksamuel.scrimage._
import tapir.model.StatusCode
import zio.{IO, UIO}

import scala.util.Random

class Terrain private() {

  private val scenarios: Seq[String] = Seq(
    "/scenario1.png",
    "/scenario2.png",
    "/scenario3.png",
    "/scenario4.png",
    "/scenario5.png",
    "/scenario6.png",
  )

  private def imageByPath(s: String): Image = Image.fromResource(s).scale(0.5)

  sealed abstract class Terrain(val rsc: Image) extends Product with Serializable
  object Terrain {
    case object Cloud extends Terrain(imageByPath("/ter-cloud.png"))
    case object Fence extends Terrain(imageByPath("/ter-fence.png"))
    case object Forest extends Terrain(imageByPath("/ter-forest.png"))
    //case object Hill extends Terrain(imageByPath("/ter-hill.png"))
    case object Obstruction extends Terrain(imageByPath("/ter-obstr.png"))
    case object Rough extends Terrain(imageByPath("/ter-rubble.png"))
    case object Rubble extends Terrain(imageByPath("/ter-rough.png"))
    case object Trench extends Terrain(imageByPath("/ter-trench.png"))
    case object Wall extends Terrain(imageByPath("/ter-wall.png"))
    case object Water extends Terrain(imageByPath("/ter-water.png"))
  }

  private val losBlocks: Seq[Terrain] =
    Seq(
      Terrain.Obstruction,
      Terrain.Obstruction,
      Terrain.Obstruction,
      Terrain.Obstruction,
      Terrain.Obstruction,
      Terrain.Obstruction,
      Terrain.Forest,
      Terrain.Forest,
      Terrain.Forest,
      Terrain.Forest,
      Terrain.Forest,
      Terrain.Forest,
      Terrain.Cloud,
      Terrain.Cloud,
      Terrain.Cloud,
      Terrain.Cloud,
      Terrain.Cloud,
      Terrain.Cloud,
    )

  private val terrains: Seq[Terrain] =
    losBlocks ++ Seq(
      Terrain.Wall,
      Terrain.Wall,
      Terrain.Wall,
      Terrain.Fence,
      Terrain.Fence,
      Terrain.Fence,
      Terrain.Rubble,
      Terrain.Rubble,
      Terrain.Rubble,
      Terrain.Rubble,
      Terrain.Water,
      Terrain.Water,
      Terrain.Water,
      Terrain.Water,
      Terrain.Trench,
      Terrain.Trench,
      Terrain.Trench,
      Terrain.Trench,
      Terrain.Rough,
      Terrain.Rough,
      Terrain.Rough,
      Terrain.Rough,
    )

  private val legend: Image = Image.fromResource("/legend.png")

  private def random[A](l: Seq[A]): A = l(Random.nextInt(l.size))

  private def randomTer(n: Int, acc: Seq[Terrain] = Seq.empty): Seq[Image] =
    if (acc.lengthCompare(n) == 0) acc map { _.rsc }
    else {
      val r = random(terrains)
      if (acc.count(_ == r) < 2) randomTer(n, r +: acc) else randomTer(n, acc)
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

  private def cluster(sc: Image): Image = {
    val tt: Seq[Image] = randomTer(8, random(losBlocks) :: Nil)
    val center = randomDev(Coord(24, 24), 2)
    val dir: Double = Random.nextInt(12) / 6.0 * π
    val xys = Seq(
      randomDev(deviation(center, 9, dir), 1),
      randomDev(deviation(center, 9, dir + π / 3.0), 1),
      randomDev(deviation(center, 9, dir + π / 1.5), 1),
      randomDev(deviation(center, 9, dir + π), 1),
      randomDev(deviation(center, 13, dir - π / 3.0), 1),
      randomDev(deviation(center, 13, dir - π / 2.0), 1),
      randomDev(deviation(center, 13, dir - π / 1.5), 1),
      center,
    )
    assert(tt.length == xys.length)
    render(sc, tt.zip(xys))
  }

  private def quadrant(sc: Image): Image = {
    val tt = randomTer(8, Nil)
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
    assert(tt.length == xys.length)
    render(sc, tt.zip(xys))
  }

  private def scatter(sc: Image): Image = {
    val vert = Random.nextBoolean()
    val tt = randomTer(if (vert) 8 else 7, random(losBlocks) :: Nil)
    val xysBase = Seq(
      randomDev(Coord(12, 14), 3),
      randomDev(Coord(12, 34), 3),
      randomDev(Coord(36, 34), 3),
      randomDev(Coord(36, 14), 3),
    )
    val xysVar =
      if (vert) Seq(
        randomDev(Coord(24, 16), 2),
        randomDev(Coord(24, 32), 2),
        randomDev(Coord(if (Random.nextBoolean()) 17 else 31, 24), 2),
      ) else Seq(
        randomDev(Coord(17, 24), 3),
        randomDev(Coord(31, 24), 3),
      )
    val xys = xysBase ++ xysVar :+ randomDev(Coord(24, 24), 2)
    assert(tt.length == xys.length)
    render(sc, tt.zip(xys))
  }

  /** Png image as byte array with default implicit writer. */
  def setupTable(n: Option[Int]): IO[StatusCode, Array[Byte]] = UIO {
    Image.fromResource(n.fold(random(scenarios)) { n => scenarios(n - 1) }).scaleTo(960, 960)
  } catchAll { _ => IO.fail(400) } map { scenario =>
    (Random.nextInt(3) match {
      case 0 => cluster(scenario)
      case 1 => quadrant(scenario)
      case 2 => scatter(scenario)
    }).bytes
  }
}

object Terrain {def apply[F[_] : Effect](): F[Terrain] = Effect[F].pure(new Terrain())}
