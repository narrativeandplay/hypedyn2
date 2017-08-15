package org.narrativeandplay.hypedyn.ui.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{extractUnmatchedPath, get, getFromFile}
import akka.stream.ActorMaterializer

import org.narrativeandplay.hypedyn.api.logging.Logger

object Server {
  private var _storyPath = ""

  private val hostname = "localhost"
  private var port = -1 // -1 represents an uninitialised port

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

  // We bind to port 0 to let Akka randomly pick an available port to bind to
  private val bindingFuture = Http().bindAndHandle(route, hostname, 0)

  bindingFuture.onFailure {
    case ex: Exception =>
      Logger.error("Server failed to start: ", ex)
  }

  bindingFuture.onSuccess {
    case binding =>
      port = binding.localAddress.getPort
      Logger.info(s"Server online at $address")
  }

  def shutdown(): Unit = {
    bindingFuture flatMap (_.unbind()) onComplete { _ => webserver.terminate() }
  }

  def storyPath = _storyPath
  def storyPath_=(s: String) = _storyPath = s

  def address = s"http://$hostname:$port"
}
