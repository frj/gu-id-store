package com.gu.idstore.guice

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import com.google.inject.Singleton

@Singleton
class StoreServlet extends HttpServlet {
  override def service(req: HttpServletRequest, resp: HttpServletResponse) {
    resp.getWriter.print("hello")
  }
}
