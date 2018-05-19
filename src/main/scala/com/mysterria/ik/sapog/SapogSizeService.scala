package com.mysterria.ik.sapog

import scala.concurrent.Future

trait SapogSizeService {
  import SapogSizeService._

  def convert(from: String, to: String, size: Double): Future[Double] = {
    val map = sizes(from).zip(sizes(to)).toMap
    map.get(size) match {
      case Some(n) => Future.successful(n)
      case None => Future.failed(new Exception("Original size is wrong, see sizes list to obtain valid values"))
    }
  }

  def list(origin: String): Seq[Double] = sizes(origin)
}

object SapogSizeService {
  private val eurSizes = Seq(40, 40.5, 41, 41.5, 42, 42.5, 43, 43.5, 44, 44.5, 45, 45.5, 46, 46.5, 47, 47.5)
  private val usaSizes = Seq(7, 7.5, 8, 8.5, 9, 9.5, 10, 10.5, 11, 11.5, 12, 12.5, 13, 13.5, 14, 14.5)
  assert(eurSizes.length == usaSizes.length, "Sizes sequences must be equal")
  val sizes = Map("eur" -> eurSizes, "usa" -> usaSizes)
  val instance: SapogSizeService = new SapogSizeService {}
}

