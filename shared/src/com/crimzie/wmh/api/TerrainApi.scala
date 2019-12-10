package com.crimzie.wmh
package api

import com.crimzie.wmh.model.Terrain
import tapir.{model => _, _}
import tapir.model.StatusCode

object TerrainApi {

  import codx.MediaTypes._

  private val terrains: EndpointInput[List[Terrain]] =
    query[List[String]]("t").map { _ map model.Terrain.param2adt } { _ map model.Terrain.adt2param }

  val genRandTerrainEndp: Endpoint[List[model.Terrain], StatusCode, Array[Byte], Nothing] =
    endpoint.get
      .in("terrain" and terrains)
      .out(body[Array[Byte], ImagePng])
      .errorOut(statusCode)

  val genTerrainEndp: Endpoint[(Int, List[model.Terrain]), StatusCode, Array[Byte], Nothing] =
    endpoint.get
      .in("terrain" / path[Int] and terrains)
      .out(body[Array[Byte], ImagePng])
      .errorOut(statusCode)

  val terrainByIdEndp: Endpoint[String, StatusCode, Array[Byte], Nothing] =
    endpoint.get
      .in("terrain" / path[String]("id"))
      .out(body[Array[Byte], ImagePng])
      .errorOut(statusCode)

}
