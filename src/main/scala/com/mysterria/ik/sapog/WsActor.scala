package com.mysterria.ik.sapog
import akka.actor.{ Actor, ActorRef, PoisonPill, Status }
import com.typesafe.scalalogging.LazyLogging

import scala.reflect.ClassTag

abstract class WsActor[M: ClassTag]() extends Actor with LazyLogging {
  import WsActor._
  private var clients: Map[Long, ActorRef] = Map.empty

  override def receive: Receive = {
    case Protocol.ConnectionOpened(connId, client) =>
      logger.trace(s"Connection $connId opened for client $client")
      clients += connId -> client
      onConnectionOpened(connId)

    case Protocol.ConnectionClosed(connId) =>
      logger.trace(s"Connection $connId closed")
      clients.get(connId) foreach { actorRef =>
        actorRef ! Status.Success(Unit)
        clients -= connId
      }

    case Protocol.MessageReceived(connId, message) =>
      logger.trace(s"Connection $connId sent a message $message")
      onMessageReceived(connId, message.asInstanceOf[M])

    case Protocol.ProtocolError(connId, cause) =>
      logger.warn(s"Connection $connId encountered protocol error: ${cause.getMessage}", cause)
      clients.get(connId) foreach { client =>
        client ! PoisonPill
      }
  }

  def dropAllExcept(except: Long): Unit = {
    clients.filterKeys(_ != except).foreach(_._2 ! PoisonPill)
  }

  def reply(connId: Long, message: Any): Unit = {
    clients.get(connId).foreach(_ ! message)
  }

  def onConnectionOpened(connId: Long): Unit = ()
  def onConnectionClosed(connId: Long): Unit = ()
  def onMessageReceived(connId: Long, message: M): Unit
}

object WsActor {
  sealed trait ProtocolMessage
  case object Protocol {
    case class ConnectionOpened(connId: Long, client: ActorRef) extends ProtocolMessage
    case class ConnectionClosed(connId: Long) extends ProtocolMessage
    case class MessageReceived[M](connId: Long, m: M) extends ProtocolMessage
    case class ProtocolError(connId: Long, cause: Throwable) extends ProtocolMessage
  }
}
