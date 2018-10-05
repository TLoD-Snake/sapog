package com.mysterria.ik.sapog

import java.util.concurrent.atomic.AtomicReference

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import javax.inject._
import org.joda.time.DateTime
import rx.lang.scala.Observable
import rx.lang.scala.subjects.PublishSubject

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class SapogNewsEditorService @Inject() (implicit as: ActorSystem, ec: ExecutionContext) extends Provider[Observable[String]] with LazyLogging {
  private val subjectRef = new AtomicReference(PublishSubject[String])
  private def subject = subjectRef.get()

  as.scheduler.schedule(1.second, 3.second) {
    logger.info(s"Emitting news in subject $subject")
    subject.onNext(s"The new sapog is on the way at ${DateTime.now().toString}")
  }

  as.scheduler.schedule(10.second, 10.second) {
    if (math.random() > 0.5) {
      subjectRef.getAndSet(PublishSubject[String]).onError(new Exception("Sum Ting Wong!"))
    } else {
      subjectRef.getAndSet(PublishSubject[String]).onCompleted()
    }
  }

  override def get(): Observable[String] = subject
}
