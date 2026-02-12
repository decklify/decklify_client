package decklify.ui

import scalafx.application.Platform
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

def showFatal(
    _title: String,
    _header: String,
    _message: String
): Nothing =
  val _ = new Alert(AlertType.Error) {
    title = _title
    headerText = _header
    contentText = _message
  }.showAndWait()
  Platform.exit()
  System.exit(1)

  throw IllegalStateException("Unreachable")

def showNonFatal(_title: String, _header: String, _message: String): Unit =
  val _ = new Alert(AlertType.Error) {
    title = _title
    headerText = _header
    contentText = _message
  }.showAndWait()
