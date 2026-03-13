package decklify.ui

import decklify.http.HttpService
import decklify.http.ServerTracker
import decklify.logic._
import decklify.model.LayoutConfig
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane

import javax.jmdns.JmmDNS

object App extends JFXApp3 {

  val WIDTH: Double = 1024
  val HEIGHT: Double = 600

  override def start(): Unit = {
    val jmdns = JmmDNS.Factory.getInstance()
    jmdns.addServiceListener("_decklify._tcp.local.", ServerTracker)

    ServerTracker.waitForServer(() => {
      showFatal("Initialization failure", "Server not found on network", "")
      throw new IllegalStateException("Server not found")
    })

    val lm = HttpService.fetchLayoutConfig
      .recover(e =>
        showFatal(
          "Initialization failure",
          "Failed to intialize the app",
          e.getMessage()
        )
        throw e
      )
      .map(LayoutFactory.fromConfig)
      .get

    val borderPane = new BorderPane { style = "-fx-background-color: #1e1e1e;" }

    lm.buildLayout

    borderPane.center <== lm.getGrid
    borderPane.setBottom(BottomBar(lm, borderPane))

    stage = new JFXApp3.PrimaryStage {
      title = "Decklify"
      scene = new Scene(WIDTH, HEIGHT) {
        root = borderPane
        fullScreen = true
      }
    }
  }
}
