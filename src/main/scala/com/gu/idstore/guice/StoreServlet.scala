package com.gu.idstore.guice

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import com.google.inject.{Inject, Singleton}
import com.gu.idstore.JsonStore
import javax.servlet._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

@Singleton
class StoreServlet @Inject()(jsonStore: JsonStore) extends Filter {
  def init(filterConfig: FilterConfig) {}
  def destroy() {}

  private val pathWithIdRegexp = """/store/([^/]+)/([^/]+)""".r

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val servletRequest = request.asInstanceOf[HttpServletRequest]
    val servletResponse = response.asInstanceOf[HttpServletResponse]
    val path = Option(servletRequest.getServletPath).getOrElse("") + Option(servletRequest.getPathInfo).getOrElse("")

    ((path, servletRequest.getMethod) match {
      case (pathWithIdRegexp(collectionName, entityId), "POST") =>
        JsonParser.parseOpt(request.getReader).map { json =>
          jsonStore.storeJson(collectionName, entityId, json)
          pair2jvalue("request" -> "worked")
        }
      case (pathWithIdRegexp(collectionName, entityId), "GET") =>
        jsonStore.getJson(collectionName, entityId)
      case _ =>
        chain.doFilter(servletRequest, servletResponse)
        None
    }).foreach { jValue =>
      response.setCharacterEncoding("UTF-8")
      response.setContentType("application/json")
      response.getWriter.append(pretty(render(jValue)))
    }
  }
}
