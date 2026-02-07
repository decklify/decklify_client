package deckt.model

import deckt.logic.TileAction
import io.circe._
import io.circe.generic.semiauto._

final case class TileConfig(
    iconName: Option[String],
    label: Option[String],
    action: TileAction
)

object TileConfig {
  given Decoder[TileConfig] = deriveDecoder
}
