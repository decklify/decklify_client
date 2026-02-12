package decklify.ui

import decklify.logic.LayoutManager
import decklify.ui.components.Clock
import decklify.ui.components.PageButton
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
