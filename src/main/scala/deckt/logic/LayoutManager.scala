package deckt.logic

import scalafx.beans.property.IntegerProperty
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.effect.GaussianBlur
import scalafx.scene.image.Image
import scalafx.scene.image.WritableImage
import scalafx.scene.layout.StackPane

import scala.collection.mutable.HashMap

class LayoutManager:
  private val tiles: HashMap[Id, Tile] = HashMap()
  private val pages = ObservableBuffer[Page]()

  private val currentPage = IntegerProperty(0)
  private val totalPage = IntegerProperty(0)

  private val currentPane = ObjectProperty(new StackPane())

  val currentBlurredBg = ObjectProperty[Image](new WritableImage(1, 1))
  val blurEffect = new GaussianBlur(80)

  val _ = pages.onChange { (_, _) =>
    totalPage.value = pages.size
  }

  val _ = currentPage.onChange { (_, _, _) =>
    currentPane.value = pages(currentPage.value).getPage
    updateBlurredBackground()
  }

  // Triggers when appending pages on startup to flush the empty GridPane
  // and to display the correct grid
  val _ = totalPage.onChange { (_, _, _) =>
    currentPane.value = pages(currentPage.value).getPage
    updateBlurredBackground()
  }

  def updateBlurredBackground(): Unit =
    if pages.isDefinedAt(currentPage.value) then
      val page = pages(currentPage.value)
      val pageBg = page.iv

      val snapshot = pageBg.snapshot(null, null)
      currentBlurredBg.value = snapshot

  def addPage(width: Int, height: Int, backgroundUrl: String): Unit =
    pages += Page(width, height, backgroundUrl)

  def addTileToPage(tile: Tile, pageNumber: Int, tileIndex: Int): Boolean =
    pages.lift(pageNumber) match {
      case Some(page) =>
        page.tiles.lift(tileIndex) match {
          case Some(None) =>
            page.tiles.update(tileIndex, Some(tile.id))
            tiles += tile.id -> tile
            true
          case _ => false
        }
      case None => false
    }

  def buildLayout: Unit = pages.foreach(_.buildPage(this))

  def getTile(tileId: Id): Option[Tile] = tiles.lift(tileId)

  def getGrid = currentPane

  def getPageNum: (IntegerProperty, IntegerProperty) = (currentPage, totalPage)

  def nextPage: Unit =
    if (currentPage.value < pages.size - 1) { currentPage.value += 1 }

  def prevPage: Unit =
    if (currentPage.value > 0) { currentPage.value -= 1 }
