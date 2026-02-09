package decklify.ui.components

import decklify.logic.LayoutManager
import scalafx.animation.ScaleTransition
import scalafx.beans.binding.Bindings
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.control.Button
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox
import scalafx.util.Duration

def glassyButton(text: String, onClick: => Unit): Button =
  new Button(text) {
    prefWidth = 150
    prefHeight = 40
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

def _prevButton(lm: LayoutManager): Button =
  glassyButton("Previous page", lm.prevPage)

def _nextButton(lm: LayoutManager): Button =
  glassyButton("Next page", lm.nextPage)

def _pageInfo(lm: LayoutManager): Label =
  val (currentPage, totalPage) = lm.getPageNum
  new Label {
    style = "-fx-text-fill: rgba(255,255,255,0.85); -fx-font-weight: bold;"
    text <== Bindings.createStringBinding(
      () => s"${currentPage.value + 1}/${totalPage.value}",
      currentPage,
      totalPage
    )
  }

def PageButton(lm: LayoutManager): HBox =
  new HBox {
    children = Seq(
      _prevButton(lm),
      _pageInfo(lm),
      _nextButton(lm)
    )
    alignment = Pos.Center
    spacing = 15
    padding = Insets(12, 12, 12, 12)
  }
