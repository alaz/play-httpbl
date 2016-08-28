package com.osinka.play.httpbl

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try
import play.api.Configuration
import play.api.mvc._
import akka.stream.Materializer
import org.slf4j.LoggerFactory

import com.osinka.httpbl.HttpBL

class HttpBLFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext, configuration: Configuration, httpBLApi: HttpBLApi) extends Filter {
  private val logger = LoggerFactory.getLogger(getClass)

  val HTTPBL_HEADER = "X-HttpBL"

  val isHeaderRequired: Boolean = configuration.getBoolean("httpbl.headers") getOrElse true

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val id = requestHeader.id
    val promise = Promise[Option[HttpBL.Response]]()
    HttpBLRequest.register(id, promise.future)

    val f = nextFilter(requestHeader)
    f.onComplete { _ => HttpBLRequest.release(id) }

    val httpbl = Try(httpBLApi.lookup(requestHeader.remoteAddress))
    promise.complete(httpbl)

    f map {
      _.withHeaders(HTTPBL_HEADER -> headerValue(httpbl.getOrElse(None)))
    }
  }

  def headerValue(response: Option[HttpBL.Response]) =
    response map {
      case r: HttpBL.SearchEngine => s"searchengine:${r.serial}"
      case r: HttpBL.Result if r.isCommentSpammer => "spammer"
      case r: HttpBL.Result if r.isHarvester => "harvester"
      case r: HttpBL.Result if r.isSuspicious => "suspicious"
    } getOrElse "none"
}
