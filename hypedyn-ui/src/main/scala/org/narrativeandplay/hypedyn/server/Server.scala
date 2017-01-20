package org.narrativeandplay.hypedyn.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{extractUnmatchedPath, get, getFromFile}
import akka.stream.ActorMaterializer

import org.narrativeandplay.hypedyn.logging.Logger

object Server {
  private var _storyPath = ""

  val hostname = "localhost"
  val port = 8080

  implicit val webserver = ActorSystem("hypedyn")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = webserver.dispatcher

  // not sure if this is a security risk
  val route =
    extractUnmatchedPath { p =>
      get {
        getFromFile(_storyPath + p.toString)
      }
    }

  private val bindingFuture = Http().bindAndHandle(route, hostname, port)

  bindingFuture.onFailure {
    case ex: Exception =>
      Logger.error("Server failed to bind to "+hostname+":"+port, ex)
  }

  bindingFuture.onSuccess {
    case _: Http.ServerBinding =>
      Logger.info("Server online at http://"+hostname+":"+port)
  }

  def shutdown(): Unit = {
    bindingFuture flatMap (_.unbind()) onComplete { _ => webserver.terminate() }
  }

  def storyPath = _storyPath
  def storyPath_=(s: String) = _storyPath = s
}
