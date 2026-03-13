package decklify.ui.utils

import scalafx.animation.ScaleTransition
import scalafx.geometry.Pos
import scalafx.scene.control.Button
import scalafx.util.Duration

def glassyButton(
    text: String,
    onClick: => Unit,
    _width: Double = 150,
    _height: Double = 40
): Button =
  new Button(text) {
    prefWidth = _width
    prefHeight = _height
    alignment = Pos.Center

    style = """
      -fx-background-color: rgba(255, 255, 255, 0.25);
      -fx-background-radius: 12;
      -fx-border-radius: 12;
      -fx-border-color: rgba(255,255,255,0.3);
      -fx-border-width: 1;
      -fx-text-fill: rgba(255,255,255,0.85);
      -fx-font-weight: bold;
      -fx-font-size: 14px;
    """

    onMouseClicked = { _ =>
      new ScaleTransition(Duration(50), this) {
        fromX = 1.0
        fromY = 1.0
        toX = 0.95
        toY = 0.95
        cycleCount = 2
        autoReverse = true
      }.play()
      onClick
    }
  }
