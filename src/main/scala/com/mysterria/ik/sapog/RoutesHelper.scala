package com.mysterria.ik.sapog

import java.util.concurrent.atomic.AtomicLong

import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.scaladsl.Flow
import com.mysterria.ik.sapog.di.RouteProvider

trait RoutesHelper extends Directives {

  def wsRoute(apath: String, handler: (Long) => Flow[Message, Message, _]): Route = {
    val conIdHolder = new AtomicLong(0L)
    pathPrefix(apath) {
      handleWebSocketMessages(handler(conIdHolder.incrementAndGet()))
    }
  }

  def foldRoutes(routes: Iterable[RouteProvider]): Route = {
    routes.map(_.route).foldLeft(RouteDirectives.reject.asInstanceOf[Route]) { _ ~ _ }
  }
}
