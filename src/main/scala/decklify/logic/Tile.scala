package decklify.logic

import decklify.http.HttpService
import decklify.ui.showNonFatal
import scalafx.animation.ScaleTransition
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Pos
import scalafx.geometry.Rectangle2D
import scalafx.scene.control.Label
import scalafx.scene.effect.DropShadow
import scalafx.scene.effect.Effect
import scalafx.scene.image.Image
import scalafx.scene.image.ImageView
import scalafx.scene.layout.StackPane
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.util.Duration

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

type Id = UUID

val RADIUS = 10

case class Tile(
    id: Id = UUID.randomUUID(),
    var iconName: Option[String],
    label: Option[String],
    action: TileAction
):
  private def sendAction: Unit =
    action match
      case TileAction.Macro(name) =>
        HttpService.sendMacroAsync(name).onComplete {
          case Success(response)  => println(response)
          case Failure(exception) =>
            Platform.runLater {
              showNonFatal(
                "Failed to execute macro",
                s"${
                    if label.isDefined then "Tile '${label}' - " else ""
                  }Macro '${name}' could not run",
                exception.getMessage()
              )
            }
        }

  private def createBlurredBgSlice(
      pane: StackPane,
      pageBg: ImageView,
      currentBlurredBg: ObjectProperty[Image],
      blurEffect: Effect
  ): ImageView =

    val bgSlice = new ImageView {
      smooth = false
      mouseTransparent = true
      effect = blurEffect
    }

    def updateViewport(): Unit = {
      val bounds = pane.layoutBounds.value
      val bgBounds = pageBg.boundsInLocal.value
      val img = currentBlurredBg.value

      if bounds.getWidth > 0 && bounds.getHeight > 0 &&
        bgBounds.getWidth > 0 && bgBounds.getHeight > 0 &&
        img != null
      then

        val tileScene = pane.localToScene(bounds)
        val bgScene = pageBg.localToScene(bgBounds)

        val dx = tileScene.getMinX - bgScene.getMinX
        val dy = tileScene.getMinY - bgScene.getMinY

        val scaleX = img.getWidth / bgBounds.getWidth
        val scaleY = img.getHeight / bgBounds.getHeight

        bgSlice.image = img
        bgSlice.viewport = new Rectangle2D(
          dx * scaleX,
          dy * scaleY,
          tileScene.getWidth * scaleX,
          tileScene.getHeight * scaleY
        )
        bgSlice.fitWidth = bounds.getWidth
        bgSlice.fitHeight = bounds.getHeight
    }

    val _ = pane.layoutBounds.onChange { (_, _, _) => updateViewport() }
    val _ = pageBg.boundsInLocal.onChange { (_, _, _) => updateViewport() }
    val _ = currentBlurredBg.onChange { (_, _, _) => updateViewport() }

    Platform.runLater(updateViewport())

    bgSlice

  private def createIconView(pane: StackPane): Option[ImageView] =
    iconName.map { name =>
      val iv = new ImageView {
        preserveRatio = true
        smooth = true
      }

      iv.fitWidth <== pane.width * 0.4
      iv.fitHeight <== pane.height * 0.4

      HttpService.fetchIconAsync(name).onComplete {
        case Success(img) => Platform.runLater { iv.image = img }
        case Failure(err) =>
          Platform.runLater {
            showNonFatal(
              "Fetch failure",
              "Failed to fetch icon",
              err.getMessage()
            )
          }
      }

      iv
    }

  private def createLabel(pane: StackPane): Label =
    val labelNode = new Label(label.get) {
      style = "-fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.85);"
      effect = new DropShadow {
        radius = 4; offsetY = 1; color = Color.rgb(0, 0, 0, 0.6)
      }
    }

    labelNode.style <== pane.width.map(w =>
      val size = Math.max(8, Math.min(20, w.floatValue() * 0.08))
      s"-fx-font-size: ${size}px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.85);"
    )

    labelNode

  private def createGlassOverlay(pane: StackPane): Rectangle =
    new Rectangle {
      width <== pane.width
      height <== pane.height
      arcWidth = RADIUS * 2
      arcHeight = RADIUS * 2
      fill = Color.rgb(255, 255, 255, 0.25)
    }

  private def createHighlight(pane: StackPane): Rectangle =
    new Rectangle {
      width <== pane.width - 2
      height <== pane.height - 2
      arcWidth = RADIUS * 2
      arcHeight = RADIUS * 2
      fill = Color.Transparent
      stroke = Color.rgb(255, 255, 255, 0.25)
      strokeWidth = 1
    }

  def buildTile(
      pageBg: ImageView,
      currentBlurredBg: ObjectProperty[Image],
      blurEffect: Effect
  ): StackPane =
    val pane = new StackPane {
      minWidth = 50
      minHeight = 50
      style = s"-fx-border-radius: ${RADIUS}; -fx-background-radius: ${RADIUS};"
      effect = new DropShadow {
        color = Color.rgb(55, 182, 255, 0.5)
        radius = RADIUS
        spread = 0.3
      }
    }

    val bgSlice =
      createBlurredBgSlice(pane, pageBg, currentBlurredBg, blurEffect)
    val content = new VBox(6) {
      alignment = Pos.Center
    }

    createIconView(pane).foreach(content.children.add(_))

    if label.isDefined then content.children.add(createLabel(pane)): Unit

    val glass = createGlassOverlay(pane)
    val highlight = createHighlight(pane)

    pane.clip = new Rectangle {
      width <== pane.width
      height <== pane.height
      arcWidth = RADIUS * 2
      arcHeight = RADIUS * 2
    }

    val _ = pane.children.addAll(bgSlice, glass, highlight, content)

    pane.onMouseClicked = _ =>
      new ScaleTransition(Duration(50), pane) {
        fromX = 1.0
        fromY = 1.0
        toX = 0.95
        toY = 0.95
        cycleCount = 2
        autoReverse = true
      }.play()
      sendAction

    pane
