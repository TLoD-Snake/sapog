package com.mysterria.ik.sapog

import com.google.inject.Guice
import com.mysterria.ik.sapog.di.SapogModule

object Boot extends App {
  Guice.createInjector(new SapogModule)
}
