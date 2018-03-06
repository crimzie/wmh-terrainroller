package com.crimzie.wmh.terrain

import com.crimzie.wmh.terrain.controllers.RollingController
import fs2.StreamApp.ExitCode
import monix.eval.Task
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import scribe.Level
// implicits:
import monix.eval.Task.{catsAsync, catsEffect}
import monix.execution.Scheduler.Implicits.global

object Server extends fs2.StreamApp[Task] with Http4sDsl[Task] {
  scribe.Logger.root.clearHandlers.withHandler(minimumLevel = Level.Info)

  val service@cats.data.Kleisli(run): HttpService[Task] = HttpService[Task] {
    case GET -> Root => RollingController.randomMethod()
  }

  override def stream(
      args: List[String],
      requestShutdown: Task[Unit],
  ): fs2.Stream[Task, ExitCode] =
    BlazeBuilder[Task]
      .bindHttp(8080, "0.0.0.0")
      .mountService(service, "/")
      .serve
}
