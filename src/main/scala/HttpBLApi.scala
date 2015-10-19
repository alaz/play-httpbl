package com.osinka.play.httpbl

import java.net.InetAddress
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import com.osinka.httpbl.HttpBL

object HttpBLApi {
  private[httpbl] var api: Option[HttpBL] = None

  def underlying: HttpBL =
    api getOrElse sys.error("No HttpBL plugin in the application")

  /**
   * Normally called by [[HttpBLFilter]] to initiate Http:BL request
   *
   * @see https://www.playframework.com/documentation/2.4.x/HTTPServer#Configure-trusted-proxies
   */
  def lookup(remoteAddress: String): Future[Option[HttpBL.Response]] =
    Future {
      val ip = sanitizeIP(remoteAddress)

      val addr = InetAddress.getByName(ip)
      if (addr.isAnyLocalAddress)
        // TODO: Makes no sense to request Http:BL for any kind of local address. Anyway should be configurable.
        None
      else
        api flatMap { _.apply(addr) }
    }

  // Play 2.3.x may give us plain `X-Forwarded-For`
  def sanitizeIP(ip: String) = ip.split(""",\s*""").filterNot("unknown".==).last
}
