package com.gu.idstore.guice

import com.google.inject.{Guice, Injector}
import com.google.inject.servlet.{ServletModule, GuiceServletContextListener}

class ContextListener extends GuiceServletContextListener {
  override def getInjector(): Injector = {
    Guice.createInjector(new ServletModule() {
        override def configureServlets() {
          serve("/store/*").`with`(classOf[StoreServlet]);
        }
    });
  }
}
