package com.crimzie.wmh
package api

import com.crimzie.wmh.model.Player
import tapir._
import tapir.model.StatusCode

object Chicken {

  val newChickenEndp: Endpoint[Player, Unit, String, Nothing] =
    endpoint.post.in("chicken" / "new" and formBody[Player]).out(htmlBodyUtf8)

  val completeChickenEndp: Endpoint[(String, Player), StatusCode, String, Nothing] =
    endpoint.post
      .in("chicken" / path[String]("id") and formBody[Player])
      .out(htmlBodyUtf8)
      .errorOut(statusCode)

  val getChickenEndp: Endpoint[String, StatusCode, String, Nothing] =
    endpoint.get.in("chicken" / path[String]("id")).out(htmlBodyUtf8).errorOut(statusCode)

}
