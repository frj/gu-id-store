package com.gu.idstore.guice

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.google.inject.{Inject, Singleton}
import com.gu.idstore.{Authentication, JsonStore}
import javax.servlet._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

@Singleton
class StoreServlet @Inject()(jsonStore: JsonStore,
                             authentication: Authentication) extends Filter {

  def init(filterConfig: FilterConfig) {}
  def destroy() {}

  private val pathWithIdRegexp = """^/store/([^/]+)/([^/]+)""".r

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val servletRequest = request.asInstanceOf[HttpServletRequest]
    val servletResponse = response.asInstanceOf[HttpServletResponse]
    val path = Option(servletRequest.getServletPath).getOrElse("") + Option(servletRequest.getPathInfo).getOrElse("")

    ((path, servletRequest.getMethod, Option(servletRequest.getParameter("method"))) match {
      case (pathWithIdRegexp(collectionName, entityId), "POST", _) => saveData(servletRequest, collectionName, entityId)
      case (pathWithIdRegexp(collectionName, entityId), _, Some("POST")) => saveData(servletRequest, collectionName, entityId)
      case (pathWithIdRegexp(collectionName, entityId), "GET", _) => getData(servletRequest, collectionName, entityId)
      case _ =>
        chain.doFilter(servletRequest, servletResponse)
        None
    }).foreach { jValue =>
      response.setCharacterEncoding("UTF-8")
      response.setContentType("application/json")
      response.getWriter.append(pretty(render(jValue)))
    }
  }

  private def saveData(request: HttpServletRequest, collectionName: String, entityId: String): Option[JValue] = {
    authentication.authenticateEntityAccess(request, entityId) match {
      case Right(_) => {
        ({
          if (request.getParameter("method") == "POST") Option(request.getParameter("body")) match {
            case None => throw new Exception("No request body provided")
            case Some(body) => JsonParser.parseOpt(body)
          }
          else JsonParser.parseOpt(request.getReader)
        }).map { json =>
          jsonStore.storeJson(collectionName, entityId, json)
          pair2jvalue("status" -> "OK")
        }
      }
      // TODO: handle different auth failures (wrong/missing) differently?
      case Left(_) => Some(pair2jvalue("status" -> "FORBIDDEN"))
    }
  }

  private def getData(request: HttpServletRequest, collectionName: String, entityId: String): Option[JValue] = {
    authentication.authenticateEntityAccess(request, entityId) match {
      case Right(_) => jsonStore.getJson(collectionName, entityId)
      // TODO: handle different auth failures (wrong/missing) differently?
      case Left(_) => jsonStore.getPublicJson(collectionName, entityId)
    }
  }
}
