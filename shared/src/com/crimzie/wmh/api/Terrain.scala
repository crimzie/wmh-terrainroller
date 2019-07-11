package com.crimzie.wmh
package api

import tapir._
import tapir.model.StatusCode

object Terrain {

  import codx.MediaTypes._

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
