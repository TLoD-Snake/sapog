package com.mysterria.ik.sapog

import play.api.libs.json.{ Format, Json }

/**
 * Translation between wired protocol message and internal app message
 * For production use it must operate with bytes array
 */
trait WiredProtocol[T] {
  def thaw(frozen: String): T
  def freeze(message: T): String
}

class JsonWiredProtocol[T: Format] extends WiredProtocol[T] {
  override def thaw(frozen: String): T = Json.parse(frozen).as[T]
  override def freeze(message: T): String = Json.stringify(Json.toJson(message))
}
