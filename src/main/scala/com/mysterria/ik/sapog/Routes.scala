package com.mysterria.ik.sapog

import java.util.concurrent.atomic.AtomicLong

import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.scaladsl.Flow

trait Routes extends Directives {

  def wsRoute(apath: String, handler: (Long) => Flow[Message, Message, _]): Route = {
    val conIdHolder = new AtomicLong(0L)
    path(apath) {
      handleWebSocketMessages(handler(conIdHolder.incrementAndGet()))
    }
  }
}
