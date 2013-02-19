package com.gu.idstore

import javax.servlet.http.HttpServletRequest
import com.google.appengine.api.datastore.Query.FilterPredicate
import java.net.URLDecoder
import com.google.appengine.api.datastore.Query.FilterOperator
import utility.{Params, Param}


object RequestFilters {

  val filterParamRegexp = """([^:]+):(.*)""".r

  def filters(request: HttpServletRequest): List[FilterPredicate] = {
    val params = Params(request.getQueryString)
    params.getParams("filter").map((filterParam) => {
      val filterParamRegexp(key, value) = filterParam.value
      new FilterPredicate(key, FilterOperator.EQUAL, value)
    })
  }
}
