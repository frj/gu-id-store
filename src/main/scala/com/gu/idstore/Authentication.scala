package com.gu.idstore

import javax.servlet.http.HttpServletRequest
import com.gu.identity.model.User
import com.google.inject.Inject
import java.net.URLDecoder
import utility.Params


class Authentication @Inject()(identityApiClientProvider: IdentityApiClientProvider) {
  val identityApiClient = identityApiClientProvider.getIdentityApiClient

  // TODO: add access token support
  def authenticate(request: HttpServletRequest): Option[User] = {
    (Params(request).getParam("GU_U") match {
      case Some(param) => Option(param.value)
      case None => {
        Option(request.getCookies).flatten.toList.filter(_.getName == "GU_U") match {
          case Nil => None
          case head :: _ => Option(head.getValue)
        }
      }
    }).map(guU => {
      identityApiClient.extractUserDataFromGuUCookie(guU)
    })
  }

  def authenticateEntityAccess(request: HttpServletRequest, entityId: String): Either[Option[User], User] = {
    authenticate(request) match {
      case Some(user) if user.getId() == entityId => Right(user)
      case Some(user) => Left(Some(user))
      case _ => Left(None)
    }
  }

  // TODO: deal with admin access for any record / general querying
  def adminAuthentication(request: HttpServletRequest): Boolean = {
    false
  }
}
