package com.mysterria.ik.sapog.di

import akka.actor.ActorSystem
import com.google.inject.name.Names
import com.google.inject.{ AbstractModule, Provides }
import com.mysterria.ik.sapog.restapi.routes.SizeRoute
import com.mysterria.ik.sapog.websocket.WebSocketService
import com.mysterria.ik.sapog.{ Constants, SapogNewsBroadcastService, SapogNewsEditorService, Server }
import com.typesafe.config.Config
import javax.inject.Singleton
import net.codingwell.scalaguice.ScalaModule
import rx.lang.scala.Observable

import scala.concurrent.ExecutionContext

class SapogModule extends AbstractModule with ScalaModule with RouteBindings {

  override def configure(): Unit = {
    binder().requireExplicitBindings()
    binder().requireAtInjectOnConstructors()
    binder().requireExactBindingAnnotations()

    bind[Server].asEagerSingleton()

    // Adding WS
    bind[WebSocketService].in[Singleton]
    routeBinder(binder).addBinding.to[WebSocketService]

    // Adding REST API modules
    bind[SizeRoute].in[Singleton]
    routeBinder(binder).addBinding.to[SizeRoute]

    bind[Observable[String]]
      .annotatedWith(Names.named("SapogNewsSubject"))
      .toProvider[SapogNewsEditorService]

    bind[SapogNewsBroadcastService].in[Singleton]
    routeBinder(binder).addBinding.to[SapogNewsBroadcastService]

    // Lets say we want to add new module with set of routes for manipulating shoes manufacturer info.
    //bind[ManufacturerRoute].in[Singleton]
    //routeBinder(binder).addBinding.to[ManufacturerRoute]
  }

  @Provides
  @Singleton
  def provideActorSystem(): ActorSystem = ActorSystem.create(Constants.AppActorSystemName)

  @Provides
  @Singleton
  def provideConfiguration(as: ActorSystem): Config = as.settings.config

  @Provides
  @Singleton
  def provideEC(as: ActorSystem): ExecutionContext = as.dispatcher
}
