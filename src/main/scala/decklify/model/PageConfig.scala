package decklify.model

import io.circe._
import io.circe.generic.semiauto._

final case class PageConfig(
    width: Int,
    height: Int,
    tiles: Seq[Option[TileConfig]],
    backgroundUrl: String
)

object PageConfig {
  given Decoder[PageConfig] = deriveDecoder
}
