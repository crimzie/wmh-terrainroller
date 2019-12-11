package com.crimzie.wmh
package api

import tapir._

object FilesApi {

  import codx.MediaTypes._

  val headerImg: Endpoint[Unit, Unit, Array[Byte], Nothing] =
    endpoint.get.in("head__img.jpg").out(body[Array[Byte], ImageJpg])

  val textureImg: Endpoint[Unit, Unit, Array[Byte], Nothing] =
    endpoint.get.in("black_texture.png").out(body[Array[Byte], ImagePng])

  val textureFooImg: Endpoint[Unit, Unit, Array[Byte], Nothing] =
    endpoint.get.in("black_texture_foo.png").out(body[Array[Byte], ImagePng])

  val styleCss: Endpoint[Unit, Unit, String, Nothing] =
    endpoint.get.in("style.css").out(body[String, TextCss])

}
