package com.osinka.play.httpbl

import scala.concurrent.{Future, Promise}
import scala.util.Try
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import org.slf4j.LoggerFactory

import com.osinka.httpbl.HttpBL

object HttpBLFilter extends Filter {
  private val logger = LoggerFactory.getLogger(getClass)

  val HTTPBL_HEADER = "X-HttpBL"

  private[httpbl] var isHeaderRequired: Boolean = false

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val id = requestHeader.id
    val promise = Promise[Option[HttpBL.Response]]()
    HttpBLRequest.register(id, promise.future)

    val f = nextFilter(requestHeader)
    f.onComplete { _ => HttpBLRequest.release(id) }

    val httpbl = Try(HttpBLApi.lookup(requestHeader.remoteAddress))
    promise.complete(httpbl)

    f map { _.withHeaders(HTTPBL_HEADER -> headerValue(httpbl.getOrElse(None))) }
  }

  def headerValue(response: Option[HttpBL.Response]) =
    response map {
      case r : HttpBL.SearchEngine => s"searchengine:${r.serial}"
      case r : HttpBL.Result if r.isCommentSpammer => "spammer"
      case r : HttpBL.Result if r.isHarvester => "harvester"
      case r : HttpBL.Result if r.isSuspicious=> "suspicious"
    } getOrElse "none"
}
