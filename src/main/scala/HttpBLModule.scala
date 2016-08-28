package com.osinka.play.httpbl

import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import org.slf4j.LoggerFactory

import com.osinka.httpbl.HttpBL

class HttpBLModule extends Module {
  private val logger = LoggerFactory.getLogger(getClass)

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =
    Seq(
      bind[HttpBLApi].to(impl(configuration))
    )

  def impl(configuration: Configuration) = {
    val apiKey = configuration.getString("httpbl.apiKey") getOrElse sys.error(s"`httpbl.apiKey` is a required configuration for Http:BL module")
    new Impl(new HttpBL(apiKey))
  }

  class Impl(override val underlying: HttpBL) extends HttpBLApi

}
