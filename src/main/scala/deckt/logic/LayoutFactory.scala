package deckt.logic

import deckt.model.LayoutConfig

object LayoutFactory {

  def fromConfig(cfg: LayoutConfig): LayoutManager = {
    val lm = new LayoutManager

    cfg.pages.zipWithIndex.foreach { (page, pageIndex) =>
      lm.addPage(page.width, page.height, page.backgroundUrl)

      page.tiles.zipWithIndex.foreach((o, tileIndex) =>
        o.foreach(t =>
          lm.addTileToPage(
            new Tile(iconName = t.iconName, label = t.label, action = t.action),
            pageIndex,
            tileIndex
          )
        )
      )
    }

    lm
  }
}
