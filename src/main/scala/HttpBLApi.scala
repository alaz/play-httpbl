package com.osinka.play.httpbl

import java.net.InetAddress

import com.osinka.httpbl.HttpBL

trait HttpBLApi {
  def underlying: HttpBL

  /**
    * Normally called by [[HttpBLFilter]] to initiate Http:BL request
    *
    * @see https://www.playframework.com/documentation/2.4.x/HTTPServer#Configure-trusted-proxies
    */
  def lookup(remoteAddress: String): Option[HttpBL.Response] = {
    val ip = sanitizeIP(remoteAddress)

    val addr = InetAddress.getByName(ip)
    if (addr.isAnyLocalAddress || addr.isSiteLocalAddress || addr.isLoopbackAddress || addr.isLinkLocalAddress)
    // TODO: Makes no sense to request Http:BL for any kind of local address. Anyway should be configurable.
      None
    else
      underlying(addr)
  }

  // Play 2.3.x may give us plain `X-Forwarded-For`
  // TODO: is is required for Play 2.5.x ?
  def sanitizeIP(ip: String) = ip.split(""",\s*""").filterNot("unknown".==).last
}

