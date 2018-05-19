package com.mysterria.ik.sapog

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.mysterria.ik.sapog.di.RouteProvider
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import javax.inject._

import scala.concurrent.Await
import scala.concurrent.duration._

class Server @Inject() (
  system: ActorSystem,
  config: Config,
  @Named("routes") routeProviders: Set[RouteProvider])(implicit as: ActorSystem) extends RoutesHelper with LazyLogging {
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val interface = config.getString("app.interface")
  private val port = config.getInt("app.port")

  Http().bindAndHandle(foldRoutes(routeProviders), interface, port)

  logger.info(s"Server online at http://$interface:$port/")
  Await.result(system.whenTerminated, Duration.Inf)
}
