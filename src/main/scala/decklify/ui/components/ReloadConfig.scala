package decklify.ui.components

import decklify.http.HttpService
import decklify.logic.LayoutFactory
import decklify.logic.LayoutManager
import decklify.ui.showNonFatal
import decklify.ui.utils.glassyButton
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.HBox

def _swapGrid(parent: BorderPane, lmObs: ObjectProperty[LayoutManager]): Unit =
  val lm = HttpService.fetchLayoutConfig
    .recover(e =>
      showNonFatal(
        "Connection error",
        "Failed to fetch the config.",
        e.getMessage()
      )
      throw e
    )
    .map(LayoutFactory.fromConfig)
    .get

  lm.buildLayout

  parent.center <== lm.getGrid

  lmObs.set(lm)

def reloadConfigButton(
    parent: BorderPane,
    lmObs: ObjectProperty[LayoutManager]
): HBox =
  val button = glassyButton(
    "Reload",
    _swapGrid(parent, lmObs),
    100
  )

  new HBox {
    children = Seq(
      button
    )
    alignment = Pos.Center
    spacing = 15
    padding = Insets(15, 12, 15, 12)
  }
