package com.osinka.play.httpbl

import scala.concurrent.Future
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import com.osinka.httpbl.HttpBL

object HttpBLFilter extends Filter {
  private val logger = LoggerFactory.getLogger(getClass)

  val HTTPBL_HEADER = "X-HttpBL"

  private[httpbl] var isHeaderRequired: Boolean = false

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val id = requestHeader.id

    val httpblReq =
      HttpBLApi.lookup(requestHeader.remoteAddress) recover {
        // do not let spill the failure to filter, we don't want our app to return 500
        case ex =>
          logger.warn(s"Failed to get Http:BL for ${requestHeader.remoteAddress} $requestHeader", ex)
          None
      }
    HttpBLRequest.register(id, httpblReq)

    val f = nextFilter(requestHeader) zip httpblReq
    f.onComplete { _ => HttpBLRequest.release(id) }

    f map {
      case (result, httpbl) =>
        result.withHeaders(HTTPBL_HEADER -> headerValue(httpbl))
    }
  }

  def headerValue(response: Option[HttpBL.Response]) =
    response map {
      case r : HttpBL.SearchEngine => s"searchengine:${r.serial}"
      case r : HttpBL.Result if r.isCommentSpammer => "spammer"
      case r : HttpBL.Result if r.isHarvester => "harvester"
      case r : HttpBL.Result if r.isSuspicious=> "suspicious"
    } getOrElse "none"
}
