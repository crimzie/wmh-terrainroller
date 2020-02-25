package com.crimzie.wmh
package ctrl

import java.awt

import cats.effect.Effect
import com.crimzie.wmh.model.Terrain
import com.sksamuel.scrimage._
import tapir.model.StatusCode
import zio.IO

import scala.util.Random

class TerrainCtrl private() {

  private val font: awt.Font = { //java
    val s = getClass.getResourceAsStream("/font.otf")
    val f = awt.Font.createFont(awt.Font.TRUETYPE_FONT, s).deriveFont(46.0f)
    s.close()
    awt.GraphicsEnvironment.getLocalGraphicsEnvironment.registerFont(f)
    f
  }

  private def textImg(txt: String): Image = Image.wrapAwt { //java
    val bi = new awt.image.BufferedImage(420, 80, 2)
    val gr = bi.createGraphics()
    gr.setPaint(awt.Color.BLACK)
    gr.setFont(font)
    gr.setRenderingHint(awt.RenderingHints.KEY_ANTIALIASING, awt.RenderingHints.VALUE_ANTIALIAS_ON)
    gr.drawString(txt, 10, 46)
    gr.dispose()
    bi
  }

  private val scenarios: Seq[Image] = {
    val f: (String, String) => Image = (rsc, name) =>
      Image.fromResource(rsc).overlay(textImg(name).scale(2), 10, 10)
    Seq(
      f("/scenario1.png", "Scenario 1: King Of The Hill"),
      f("/scenario2.png", "Scenario 2: Bunkers"),
      f("/scenario3.png", "Scenario 3: Spread The Net"),
      f("/scenario4.png", "Scenario 4: Invasion"),
      f("/scenario5.png", "Scenario 5: Anarchy"),
      f("/scenario6.png", "Scenario 6: Recon II"),
    )
  }

  private val ter2icon: Map[Terrain, Image] = Map(
    Terrain.Forest -> Image.fromResource("/ter-forest.png").scale(0.5),
    Terrain.Cloud -> Image.fromResource("/ter-cloud.png").scale(0.5),
    Terrain.Obstruction -> Image.fromResource("/ter-obstr.png").scale(0.5),
    Terrain.Fence -> Image.fromResource("/ter-fence.png").scale(0.5),
    Terrain.Rough -> Image.fromResource("/ter-rough.png").scale(0.5),
    Terrain.Rubble -> Image.fromResource("/ter-rubble.png").scale(0.5),
    Terrain.Trench -> Image.fromResource("/ter-trench.png").scale(0.5),
    Terrain.Wall -> Image.fromResource("/ter-wall.png").scale(0.5),
    Terrain.Water -> Image.fromResource("/ter-water.png").scale(0.5),
    Terrain.Acid -> Image.fromResource("/ter-acid.png").scale(0.5),
    Terrain.AcidCloud -> Image.fromResource("/ter-ccloud.png").scale(0.5),
    Terrain.FireCloud -> Image.fromResource("/ter-bcloud.png").scale(0.5),
    Terrain.FireForest -> Image.fromResource("/ter-bforest.png").scale(0.5),
    Terrain.FireRubble -> Image.fromResource("/ter-brubble.png").scale(0.5),
    Terrain.Other -> Image.fromResource("/ter-custom.png").scale(0.5),
  )

  private val ter2label: Map[Terrain, Image] =
    Terrain.list.map { t => t -> textImg(Terrain.adt2name(t)) }(scala.collection.breakOut)

  private val losBlocks: Seq[Terrain] =
    Seq(
      model.Terrain.Obstruction,
      model.Terrain.Obstruction,
      model.Terrain.Forest,
      model.Terrain.Forest,
      model.Terrain.Cloud,
      model.Terrain.Cloud,
    )

  private val terrains: Seq[Terrain] =
    losBlocks ++ Seq(
      model.Terrain.Wall,
      model.Terrain.Wall,
      model.Terrain.Fence,
      model.Terrain.Fence,
      model.Terrain.Rubble,
      model.Terrain.Rubble,
      model.Terrain.Water,
      model.Terrain.Water,
      model.Terrain.Trench,
      model.Terrain.Trench,
      model.Terrain.Rough,
      model.Terrain.Rough,
    )

  private val legendY: Int = (Terrain.list.size / 2.0).ceil.toInt * 80

  private val legend: Image = {
    def rec(in: Iterator[(Seq[Terrain], Int)], acc: Image): Image =
      if (in.hasNext) {
        val (l, n) = in.next
        val y = 10 + 80 * n
        val wLeft =
          acc.overlay(ter2icon(l.head), 10, y).overlay(ter2label(l.head), 80, y)
        val img =
          (wLeft /: l.tail.headOption) { (i, t) =>
            i.overlay(ter2icon(t), 490, y).overlay(ter2label(t), 560, y)
          }
        rec(in, img)
      } else acc

    rec(Terrain.list.sliding(2, 2).zipWithIndex, Image(960, legendY))
  }

  private def random[A](l: Seq[A]): A = l(Random.nextInt(l.size))

  private def shuffleUpLB(l: Seq[Terrain]): Seq[Image] = {
    val bi = l.indexWhere { _.isInstanceOf[model.Terrain.LosBlock] }
    val btt =
      if (bi > -1) l(bi) +: Random.shuffle(l.take(bi) ++ l.drop(bi + 1)) else Random.shuffle(l)
    btt map ter2icon
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
      .resizeTo(960, 960 + legendY, Position.TopCenter, Color.White)
      .overlay(legend, y = 960)

  private def cluster(sc: Image, tt: Seq[Terrain]): Image = {
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

  private def quadrant(sc: Image, tt: Seq[Terrain]): Image = {
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
    render(sc, Random.shuffle(tt) map ter2icon zip xys)
  }

  private def scatter(sc: Image, tt: Seq[Terrain]): Image = {
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
  def setupTable(n: Option[Int])(ott: Option[Seq[Terrain]]): IO[StatusCode, Array[Byte]] = IO {
    n.fold(random(scenarios)) { n => scenarios(n - 1) }.scaleTo(960, 960)
  } catchAll { _ => IO.fail(400) } map { scenario =>
    val tt = ott getOrElse terrains
    (Random.nextInt(3) match {
      case 0 => cluster(scenario, tt)
      case 1 => quadrant(scenario, tt)
      case 2 => scatter(scenario, tt)
    }).bytes
  }
}

object TerrainCtrl {def apply[F[_] : Effect](): F[TerrainCtrl] = Effect[F].delay(new TerrainCtrl())}
