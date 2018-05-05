package com.mysterria.ik.sapog

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Server extends App with Routes with LazyLogging {
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val wp = new JsonWiredProtocol[WiredMessage]
  val wsEndpoint = new WsEndpoint(wp, new WsActor[WiredMessage] {
    override def onConnectionOpened(connId: Long): Unit = {
      dropAllExcept(connId)
    }
    override def onMessageReceived(connId: Long, message: WiredMessage): Unit = {
      reply(connId, WiredMessage(message.foo + 1))
    }
  })
  lazy val routes: Route = wsRoute("ws", wsEndpoint.websocketFlow)

  Http().bindAndHandle(routes, "localhost", 8080)

  logger.info(s"Server online at http://localhost:8080/")
  Await.result(system.whenTerminated, Duration.Inf)
}
