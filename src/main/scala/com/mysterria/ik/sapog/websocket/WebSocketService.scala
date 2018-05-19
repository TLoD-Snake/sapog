package com.mysterria.ik.sapog.websocket

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import com.mysterria.ik.sapog._
import com.mysterria.ik.sapog.di.RouteProvider
import javax.inject.Inject
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class WebSocketService @Inject() (implicit ec: ExecutionContext, system: ActorSystem) extends RoutesHelper with RouteProvider {

  val wp = new JsonWiredProtocol[WiredMessage]
  val wsEndpoint = new WsEndpoint(wp, new WsActor[WiredMessage] {

    //TODO: To be more precise init scheduler personally for each newly opened connection
    system.scheduler.schedule(10.second, 10.second) {
      replyAll(Json.obj("time" -> System.currentTimeMillis() / 1000))
    }

    override def onConnectionOpened(connId: Long): Unit = {
      dropAllExcept(connId, Some(Json.obj("error" -> "duplicate")))
    }

    override def onMessageReceived(connId: Long, message: WiredMessage): Unit = {
      reply(connId, WiredMessage(message.foo + 1))
    }

    override def onProtocolError(connId: Long, cause: Throwable): Unit = {
      drop(connId, Some(Json.obj("error" -> "input", "details" -> cause.getMessage)))
    }
  })

  override lazy val route: Route = wsRoute("ws", wsEndpoint.websocketFlow)
}
