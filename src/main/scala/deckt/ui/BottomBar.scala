package deckt.ui

import deckt.logic.LayoutManager
import deckt.ui.components.Clock
import deckt.ui.components.PageButton
import scalafx.geometry.Insets
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.Region

def BottomBar(lm: LayoutManager): BorderPane =
  val clock = Clock()

  new BorderPane {
    padding = Insets(5)
    center = PageButton(lm)
    right = clock
    left = new Region {
      prefWidth <== clock.width
    }
  }
