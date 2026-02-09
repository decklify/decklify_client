package decklify.ui

import decklify.http.HttpService
import decklify.logic._
import decklify.model.LayoutConfig
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane

object App extends JFXApp3 {

  val WIDTH: Double = 1024
  val HEIGHT: Double = 600

  override def start(): Unit = {
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
    borderPane.setBottom(BottomBar(lm))

    stage = new JFXApp3.PrimaryStage {
      title = "Decklify"
      scene = new Scene(WIDTH, HEIGHT) {
        root = borderPane
        fullScreen = true
      }
    }
  }
}
