package com.mysterria.ik.sapog

import akka.actor.PoisonPill
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Flow, Sink, Source }
import com.mysterria.ik.sapog.di.RouteProvider
import com.typesafe.scalalogging.LazyLogging
import javax.inject._
import rx.lang.scala.Observable

import scala.concurrent.ExecutionContext
import scala.util.Failure

/**
 * This is one of the broadcast services it is not tightly coupled with any other service and can be enabled or disabled
 * at any time with no other service code being affected
 */
class SapogNewsBroadcastService @Inject() (@Named("SapogNewsSubject") newsSubject: Provider[Observable[String]])(implicit ec: ExecutionContext)
  extends RouteProvider with RoutesHelper with Directives with LazyLogging {

  override lazy val route: Route = wsRoute("ws/ntf", routeHandler)

  def routeHandler(clientId: Long): Flow[Message, Message, _] = {
    val out = Source.actorRef[Message](256, OverflowStrategy.fail).mapMaterializedValue { out =>
      newsSubject.get.subscribe(
        onNext = v =>
          out ! TextMessage(s"Cliento $clientId comprendo: $v"),
        onError = { t =>
          out ! TextMessage(s"Cliento $clientId. Loshate mi contaaaaaare! ${t.getMessage}")
          out ! PoisonPill
        },
        onCompleted = () => {
          out ! TextMessage(s"Cliento $clientId. Finita la comedia.")
          out ! PoisonPill
        })
    }.watchTermination() { (sub, f) => f.onComplete(_ => sub.unsubscribe()) }
    Flow
      .fromSinkAndSourceCoupled(Sink.ignore, out)
      .watchTermination() { (_, f) =>
        f onComplete {
          case Failure(cause) =>
            logger.error(s"WS stream $this failed with ${cause.getMessage}", cause)
          case _ =>
            logger.trace(s"Flow $this regular completion")
        }
      }
  }
}
