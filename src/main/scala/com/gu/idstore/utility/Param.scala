package com.gu.idstore.utility

import java.net.URLDecoder
import javax.servlet.http.HttpServletRequest

case class Param(name: String, value: String)

case class Params(params: List[Param]) {
  def getParam(name: String): Option[Param] = params.find(_.name == name)
  def getParams(name: String): List[Param] = params.filter(_.name == name)
}
object Params {
  def apply(queryString: String): Params = {
    Params(queryString.split("&").filter(param => param.contains("=")).map((param: String) => {
      var split = param.split("=")
      Param(URLDecoder.decode(split(0), "UTF-8"), URLDecoder.decode(split(1), "UTF-8"))
    }).toList)
  }

  def apply(request: HttpServletRequest): Params = Params(request.getQueryString)
}
