package decklify.ui.components

import decklify.logic.LayoutManager
import decklify.ui.utils.glassyButton
import scalafx.beans.binding.Bindings
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.control.Button
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox

def _prevButton(lm: ObjectProperty[LayoutManager]): Button =
  glassyButton("Previous page", lm.get.prevPage)

def _nextButton(lm: ObjectProperty[LayoutManager]): Button =
  glassyButton("Next page", lm.get.nextPage)

def _pageInfo(lm: ObjectProperty[LayoutManager]): Label =
  new Label {
    style = "-fx-text-fill: rgba(255,255,255,0.85); -fx-font-weight: bold;"

    def rebind(): Unit =
      val (currentPage, totalPage) = lm.get.getPageNum
      text <== Bindings.createStringBinding(
        () => s"${currentPage.value + 1}/${totalPage.value}",
        currentPage,
        totalPage
      )

    rebind()

    lm.addListener((_, _, _) => rebind())
  }

def PageButton(lm: ObjectProperty[LayoutManager]): HBox =
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
