package com.gu.idstore.guice

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.google.inject.{Inject, Singleton}
import com.gu.idstore.{Authentication, JsonStore}
import javax.servlet._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import com.gu.idstore.utility.{Param, Params}

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
    val params = Params(servletRequest)

    ((path, servletRequest.getMethod, params.getParam("method")) match {
      case (pathWithIdRegexp(collectionName, entityId), "POST", _) => saveData(servletRequest, collectionName, entityId)
      case (pathWithIdRegexp(collectionName, entityId), _, Some(Param("method", "POST"))) => saveData(servletRequest, collectionName, entityId)
      case (pathWithIdRegexp(collectionName, entityId), "GET", _) => getData(servletRequest, collectionName, entityId)
      case _ =>
        chain.doFilter(servletRequest, servletResponse)
        None
    }).foreach { jValue =>
      response.setCharacterEncoding("UTF-8")
      response.setContentType("application/json")
      val responseBody = params.getParam("callback") match {
        case None => {
          response.setContentType("application/json")
          pretty(render(jValue))
        }
        case Some(callback) => {
          response.setContentType("application/javascript")
          callback.value + "(" + compact(render(jValue)) + ")"
        }
      }
      response.getWriter.append(responseBody)
    }
  }

  private def saveData(request: HttpServletRequest, collectionName: String, entityId: String): Option[JValue] = {
    val params = Params(request)
    authentication.authenticateEntityAccess(request, entityId) match {
      case Right(user) => {
        ({
          params.getParam("method") match {
            case Some(Param(_, "POST")) => {
              params.getParam("body") match {
                case None => throw new Exception("Missing required param, body")
                case Some(Param(_, body)) => JsonParser.parseOpt(body)
              }
            }
            case None => JsonParser.parseOpt(request.getReader)
            case _ => throw new Exception("Unsupported method")
          }
        }).map { json =>
          jsonStore.storeJson(collectionName, user.getId(), json)
          pair2jvalue("status" -> "OK")
        }
      }
      // TODO: handle different auth failures (wrong/missing) differently?
      case Left(_) => Some(pair2jvalue("status" -> "FORBIDDEN"))
    }
  }

  private def getData(request: HttpServletRequest, collectionName: String, entityId: String): Option[JValue] = {
    authentication.authenticateEntityAccess(request, entityId) match {
      case Right(user) => jsonStore.getJson(collectionName, user.getId())
      // TODO: handle different auth failures (wrong/missing) differently?
      case Left(_) => jsonStore.getPublicJson(collectionName, entityId)
    }
  }
}
