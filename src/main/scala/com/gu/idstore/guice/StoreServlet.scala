package com.gu.idstore.guice

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import com.google.inject.{Inject, Singleton}
import com.gu.idstore.datastore.Datastore

@Singleton
class StoreServlet @Inject()(val datastore: Datastore) extends HttpServlet {
  override def service(req: HttpServletRequest, resp: HttpServletResponse) {
    resp.getWriter.print("hello")
    val data = List(("test2", 1234))
    datastore.save("testCollection", "1234", data)
    val record = datastore.get("testCollection", "1234")
    println(record)
  }
}
