package com.mysterria.ik.sapog.di

import akka.http.scaladsl.server.Route

trait RouteProvider {
  def route: Route
}
