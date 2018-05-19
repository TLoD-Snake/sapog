package com.mysterria.ik.sapog.restapi

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import com.mysterria.ik.sapog.restapi.routes.SizeRoute

/**
 * Collects all Rest API routes. You surely should use DI in real life ;)
 */
class RestApiHttpRoute(as: ActorSystem) {
  val route: Route = new SizeRoute(as).route
}
