package com.mysterria.ik.sapog

import play.api.libs.json.{ Json, OFormat }

case class WiredMessage(foo: Int)

object WiredMessage {
  implicit val jFormat: OFormat[WiredMessage] = Json.format[WiredMessage]
}