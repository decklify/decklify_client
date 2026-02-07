package deckt.ui.utils

import scalafx.scene.image.ImageView
import scalafx.scene.layout.Region

object ImageCover:

  /** Makes an ImageView behave like CSS `background-size: cover`
    *
    * @param iv
    *   The image view
    * @param container
    *   The container which contains the image
    */
  def bind(iv: ImageView, container: Region): Unit =

    iv.preserveRatio = true
    iv.smooth = true

    def updateFit(): Unit =
      val img = iv.image.value
      if img != null then
        val cw = container.width.value
        val ch = container.height.value
        val iw = img.getWidth
        val ih = img.getHeight

        if iw > 0 && ih > 0 then
          val scale = Math.max(cw / iw, ch / ih)
          iv.fitWidth = iw * scale
          iv.fitHeight = ih * scale

    updateFit()

    val _ = container.width.onChange((_, _, _) => updateFit())
    val _ = container.height.onChange((_, _, _) => updateFit())
    val _ = iv.image.onChange((_, _, _) => updateFit())
