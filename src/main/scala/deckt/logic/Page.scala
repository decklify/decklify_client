package deckt.logic

import deckt.ui.utils.ImageCover
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.image.ImageView
import scalafx.scene.layout.ColumnConstraints
import scalafx.scene.layout.GridPane
import scalafx.scene.layout.Priority
import scalafx.scene.layout.RowConstraints
import scalafx.scene.layout.StackPane
import scalafx.scene.shape.Rectangle

val GAP_SIZE = 20

class Page(_width: Int, _height: Int, backgroundUrl: String):
  require(_width > 0, "Page width must be bigger than 0.")
  require(_height > 0, "Page height must be bigger than 0.")

  val tiles: Array[Option[Id]] =
    Array.fill(_width * _height)(None)

  private val grid = new GridPane {
    hgap = GAP_SIZE
    vgap = GAP_SIZE
    padding = Insets(GAP_SIZE)
    alignment = Pos.Center
  }

  val iv = new ImageView(backgroundUrl) {
    managed = false
    smooth = true
  }

  private val pane = new StackPane()

  def buildPage(lm: LayoutManager): Unit =
    tiles.zipWithIndex.foreach { (opt, i) =>
      opt.foreach { tileId =>
        val tilePane = lm
          .getTile(tileId)
          .get
          .buildTile(
            pageBg = iv,
            currentBlurredBg = lm.currentBlurredBg,
            blurEffect = lm.blurEffect
          )
        val row = i / _width
        val col = i % _width
        grid.add(tilePane, col, row)

      }
    }

    for _ <- 0 until _width do
      grid.columnConstraints.add(new ColumnConstraints {
        percentWidth = 100.0 / _width
        hgrow = Priority.Always
      })

    for _ <- 0 until _height do
      grid.rowConstraints.add(new RowConstraints {
        percentHeight = 100.0 / _height
        vgrow = Priority.Always
      })

    pane.clip = new Rectangle {
      width <== pane.width
      height <== pane.height
    }

    ImageCover.bind(iv, pane)

    val _ = pane.children.addAll(iv, grid)

  def getPage = pane
