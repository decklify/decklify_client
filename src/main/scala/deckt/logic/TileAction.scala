package deckt.logic

import io.circe._
import io.circe.generic.semiauto._

enum TileAction {
  case Macro(name: String)
  // TODO: add actions if needed
}

object TileAction {
  given Decoder[TileAction] = deriveDecoder
}
