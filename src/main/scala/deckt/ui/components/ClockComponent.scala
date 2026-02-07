package deckt.ui.components

import scalafx.animation.KeyFrame
import scalafx.animation.Timeline
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color
import scalafx.util.Duration

import java.time.LocalTime
import java.time.format.DateTimeFormatter

def Clock(): HBox =
  val label = new Label {
    textFill = Color.White
    style = "-fx-font-size: 16px; -fx-font-weight: bold;"
  }

  val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

  val clock = new Timeline {
    cycleCount = Timeline.Indefinite
    keyFrames = Seq(
      KeyFrame(
        Duration.Zero,
        onFinished = _ => label.text = LocalTime.now().format(formatter)
      ),
      KeyFrame(Duration(500))
    )
  }

  clock.play()

  new HBox {
    children = Seq(
      label
    )
    alignment = Pos.Center
    spacing = 15
    padding = Insets(15, 12, 15, 12)
  }
