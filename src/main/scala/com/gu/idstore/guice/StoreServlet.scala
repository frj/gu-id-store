package com.gu.idstore.guice

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import com.google.inject.{Inject, Singleton}
import com.gu.idstore.JsonStore

@Singleton
class StoreServlet @Inject()(jsonStore: JsonStore) extends HttpServlet {
  override def service(req: HttpServletRequest, resp: HttpServletResponse) {
    resp.getWriter.print("hello")
  }
}
