package com.crimzie.wmh
package api

import tapir._
import tapir.model.StatusCode

object Terrain {

  private case class ImagePng() extends MediaType {
    override val mediaTypeNoParams = "image/png"
  }

  private implicit val imagePngCodec: Codec[Array[Byte], ImagePng, _] =
    Codec.byteArrayCodec.mediaType(ImagePng())

  val genTerrainEndp: Endpoint[Option[Int], StatusCode, Array[Byte], Nothing] =
    endpoint.get
      .in("terrain" and query[Option[Int]]("sc"))
      .out(body[Array[Byte], ImagePng])
      .errorOut(statusCode)

  val terrainByIdEndp: Endpoint[String, StatusCode, Array[Byte], Nothing] =
    endpoint.get
      .in("terrain" / path[String]("id"))
      .out(body[Array[Byte], ImagePng])
      .errorOut(statusCode)

}
