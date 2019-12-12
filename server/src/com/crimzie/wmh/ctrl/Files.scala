package com.crimzie.wmh
package ctrl

import cats.effect.Effect
import com.sksamuel.scrimage.Image

import scala.io.Source

class Files private() {
  
  val headerImg: Array[Byte] = Image.fromResource("/head__img.jpg").bytes

  val textureImg: Array[Byte] = Image.fromResource("/black_texture.png").bytes

  val textureFooImg: Array[Byte] = Image.fromResource("/black_texture_foo.png").bytes

  val styleCss: String = Source.fromResource("style.css").getLines.mkString

}

object Files {def apply[F[_] : Effect](): F[Files] = Effect[F].delay(new Files())}
