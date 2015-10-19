package com.osinka.play.httpbl

import org.slf4j.LoggerFactory
import play.api.{Application, Plugin}

import com.osinka.httpbl.HttpBL

class HttpBLPlugin(app: Application) extends Plugin {
  private val logger = LoggerFactory.getLogger(getClass)

  override def onStart(): Unit = {
    app.configuration.getString("httpbl.apiKey") match {
      case None =>
        logger.warn(s"`httpbl.apiKey` is a required configuration for Http:BL plugin")

      case Some(apiKey) =>
        HttpBLApi.api = Some(new HttpBL(apiKey))
        HttpBLFilter.isHeaderRequired = app.configuration.getBoolean("httpbl.headers") getOrElse true
    }
  }
}