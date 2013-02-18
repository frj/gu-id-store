package com.gu.idstore

import javax.servlet.http.HttpServletRequest
import com.google.appengine.api.datastore.Query.FilterPredicate
import java.net.URLDecoder
import com.google.appengine.api.datastore.Query.FilterOperator

case class Param(name: String, value: String)

object RequestFilters {

  val filterParamRegexp = """([^:]+):(.*)""".r

  def filters(request: HttpServletRequest): List[FilterPredicate] = {
    val params = parseQueryString(request.getQueryString)
    params.filter({_.name == "filter"}).map((filterParam) => {
      val filterParamRegexp(key, value) = filterParam.value
      new FilterPredicate(key, FilterOperator.EQUAL, value)
    })
  }

  private def parseQueryString(querystring: String): List[Param] = {
    querystring.split("&").filter(param => param.contains("=")).map((param: String) => {
      var split = param.split("=")
      Param(URLDecoder.decode(split(0), "UTF-8"), URLDecoder.decode(split(1), "UTF-8"))
    }).toList
  }
}
