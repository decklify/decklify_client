package decklify.ui

import decklify.logic.LayoutManager
import decklify.ui.components.Clock
import decklify.ui.components.PageButton
import decklify.ui.components.reloadConfigButton
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Insets
import scalafx.scene.layout.BorderPane

def BottomBar(lm: LayoutManager, parent: BorderPane): BorderPane =
  val lmObs = ObjectProperty(lm)
  val clock = Clock()
  val reload = reloadConfigButton(parent, lmObs)

  new BorderPane {
    padding = Insets(5)
    center = PageButton(lmObs)
    right = clock
    left = reload
  }
