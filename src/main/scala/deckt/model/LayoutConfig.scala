package deckt.model

import io.circe._
import io.circe.generic.semiauto._

case class LayoutConfig(
    pages: Seq[PageConfig]
)

object LayoutConfig {
  given Decoder[LayoutConfig] = deriveDecoder
}
