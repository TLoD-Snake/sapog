package com.mysterria.ik.sapog.restapi.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpEntity, HttpResponse, ResponseEntity }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import com.mysterria.ik.sapog.SapogSizeService
import com.mysterria.ik.sapog.restapi.ErrorResponse
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import play.api.libs.json.{ Json, OFormat }

import scala.concurrent.ExecutionContext

class SizeRoute(actorSystem: ActorSystem) extends PlayJsonSupport {
  import SizeRoute._
  import akka.http.scaladsl.model.StatusCodes._

  implicit val ec: ExecutionContext = actorSystem.dispatcher
  private val sizeService = SapogSizeService.instance

  val route: Route = pathPrefix("size") {
    handleExceptions(eh) {
      pathPrefix("list") {
        path(Segment ~ Slash.?) { origin => // Don't use  `~ Slash.?` in real application! Only for demo!
          get {
            complete(SizeListResponse(sizeService.list(origin)))
          }
        }
      } ~
        pathPrefix("convert") {
          pathEndOrSingleSlash {
            post {
              entity(as[SizeConversionRequest]) { r =>
                val f = sizeService.convert(r.from, r.to, r.size) map { n =>
                  OK -> SizeConversionResponse(r.from, r.to, r.size, n)
                }
                complete(f)
              }
            }
          }
        }
    }
  }
}

object SizeRoute {
  import akka.http.scaladsl.model.StatusCodes._

  val eh = ExceptionHandler {
    case t: Throwable =>
      extractUri { uri =>
        //println(s"Request to $uri could not be handled normally")
        val error = ErrorResponse(t.getMessage)
        val e = HttpEntity(Json.stringify(Json.toJson(error)))
        complete(HttpResponse(InternalServerError, entity = e))
      }
  }

  case class SizeListResponse(sizes: Seq[Double])
  case object SizeListResponse {
    implicit val jsonFormat: OFormat[SizeListResponse] = Json.format[SizeListResponse]
  }

  case class SizeConversionRequest(from: String, to: String, size: Double)
  case object SizeConversionRequest {
    implicit val jsonFormat: OFormat[SizeConversionRequest] = Json.format[SizeConversionRequest]
  }

  case class SizeConversionResponse(from: String, to: String, original: Double, converted: Double)
  case object SizeConversionResponse {
    implicit val jsonFormat: OFormat[SizeConversionResponse] = Json.format[SizeConversionResponse]
  }
}

