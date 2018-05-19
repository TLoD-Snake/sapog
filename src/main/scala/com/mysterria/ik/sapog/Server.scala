package com.mysterria.ik.sapog

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.mysterria.ik.sapog.restapi.RestApiHttpRoute
import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.Json

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._
import scala.concurrent.duration.Duration

object Server extends App with Routes with LazyLogging {
  implicit val system: ActorSystem = ActorSystem(Constants.AppActorSystemName)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  val config = system.settings.config
  val interface = config.getString("app.interface")
  val port = config.getInt("app.port")

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

  lazy val routes: Route =
    wsRoute("ws", wsEndpoint.websocketFlow) ~
      new RestApiHttpRoute(system).route

  Http().bindAndHandle(routes, interface, port)

  logger.info(s"Server online at http://$interface:$port/")
  Await.result(system.whenTerminated, Duration.Inf)
}
