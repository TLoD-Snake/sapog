package com.mysterria.ik.sapog

import akka.NotUsed
import akka.actor.{ ActorSystem, Props }
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Flow, Sink, Source }
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scala.util.Failure

class WsEndpoint[M: ClassTag](wp: WiredProtocol[M], anactor: => WsActor[M])(implicit as: ActorSystem) extends LazyLogging {
  implicit val ec: ExecutionContext = as.dispatcher

  lazy private val actor = as.actorOf(Props(anactor))

  protected def actorInSink(connId: Long): Sink[WsActor.ProtocolMessage, NotUsed] = Sink.actorRef[WsActor.ProtocolMessage](actor, WsActor.Protocol.ConnectionClosed(connId))

  protected def endPointFlow(connId: Long): Flow[String, M, Any] = {
    val in =
      Flow[String]
        .map { s =>
          try {
            WsActor.Protocol.MessageReceived(connId, wp.thaw(s))
          } catch {
            case t: Throwable => WsActor.Protocol.ProtocolError(connId, t)
          }
        }
        .to(actorInSink(connId))

    val out =
      Source.actorRef[M](1, OverflowStrategy.fail)
        .mapMaterializedValue(actor ! WsActor.Protocol.ConnectionOpened(connId, _))

    Flow.fromSinkAndSource(in, out)
  }

  def websocketFlow(connId: Long): Flow[Message, Message, Any] =
    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) => msg
      }
      .via(endPointFlow(connId))
      .map {
        case msg: M => TextMessage.Strict(wp.freeze(msg))
      }
      .via(reportErrorsFlow)

  protected def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          logger.error(s"WS stream failed with ${cause.getMessage}", cause)
        case _ =>
          logger.trace(s"Flow $this regular completion")
      })

}
