package com.mysterria.ik.sapog.restapi

import play.api.libs.json.{ Json, OFormat }

case class ErrorResponse(message: String)

case object ErrorResponse {
  implicit val jsonFormat: OFormat[ErrorResponse] = Json.format[ErrorResponse]
}
