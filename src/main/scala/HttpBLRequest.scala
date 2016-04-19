package com.osinka.play.httpbl

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.Future
import play.api.mvc.RequestHeader

import com.osinka.httpbl.HttpBL

object HttpBLRequest {
  private lazy val registry = new ConcurrentHashMap[Long,Future[Option[HttpBL.Response]]]()

  private[httpbl] var api: Option[HttpBL] = None

  def underlying: HttpBL =
    api getOrElse sys.error("No HttpBL plugin in the application")

  /**
   * Allows calling `HttpBLRequest(requestHeader)` to get the response right in a controller.
   *
   * @see https://www.playframework.com/documentation/2.4.x/HTTPServer#Configure-trusted-proxies
   */
  def apply(requestHeader: RequestHeader): Future[Option[HttpBL.Response]] =
    Option(registry.get(requestHeader.id)) getOrElse sys.error(s"Cannot find HttpBL result for request ID ${requestHeader.id}. Is HttpBL filter configured in Global?")

  def register(id: Long, f: Future[Option[HttpBL.Response]]): Unit = {
    registry.put(id, f)
  }
  
  def release(id: Long): Unit = {
    registry.remove(id)
  }
}
