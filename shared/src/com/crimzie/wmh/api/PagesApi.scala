package com.crimzie.wmh
package api

import tapir._

object PagesApi {

  val index: Endpoint[Unit, Unit, String, Nothing] =
    endpoint.get.in("").out(htmlBodyUtf8)

  val newChickenPageEndp: Endpoint[Unit, Unit, String, Nothing] =
    endpoint.get.in("chicken").out(htmlBodyUtf8)

}
