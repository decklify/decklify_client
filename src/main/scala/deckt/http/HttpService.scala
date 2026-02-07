package deckt.http

import deckt.model.LayoutConfig
import io.circe.parser.decode
import scalafx.scene.image.Image

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.jdk.FutureConverters._
import scala.util.Try

object HttpService {
  private val BASE_URL = "http://pablo.local:8000"
  private val LAYOUT_URI = URI.create(BASE_URL + "/layout")
  private val MACRO_URI = (macroName: String) =>
    URI.create(BASE_URL + s"/macro/${macroName}")
  private val ICON_URL = (iconName: String) =>
    URI.create(BASE_URL + s"/assets/icons/${iconName}.png")

  private val client = HttpClient.newBuilder
    .connectTimeout(Duration.ofSeconds(5))
    .version(HttpClient.Version.HTTP_1_1)
    .build()

  def fetchLayoutConfigAsync: Future[LayoutConfig] =
    val request = HttpRequest
      .newBuilder()
      .uri(LAYOUT_URI)
      .GET()
      .timeout(Duration.ofSeconds(5))
      .build()

    client
      .sendAsync(request, BodyHandlers.ofString())
      .asScala
      .flatMap { response =>
        decode[LayoutConfig](response.body()) match {
          case Right(cfg) => Future.successful(cfg)
          case Left(err)  =>
            Future.failed(
              new Exception(s"Failed to parse JSON: ${err.toString()}")
            )
        }
      }

  def sendMacroAsync(macroName: String): Future[String] =
    val request = HttpRequest
      .newBuilder()
      .uri(MACRO_URI(macroName))
      .POST(BodyPublishers.noBody())
      .timeout(Duration.ofSeconds(5))
      .build()

    client
      .sendAsync(request, BodyHandlers.ofString())
      .asScala
      .map(_.body())

  def fetchLayoutConfig: Try[LayoutConfig] =
    Try {
      Await.result(fetchLayoutConfigAsync, 5.seconds)
    }

  def fetchIconAsync(iconName: String): Future[Image] =
    val request = HttpRequest
      .newBuilder()
      .uri(ICON_URL(iconName))
      .GET()
      .timeout(Duration.ofSeconds(5))
      .build()

    client
      .sendAsync(request, BodyHandlers.ofByteArray())
      .asScala
      .flatMap { response =>
        if (response.statusCode() == 200) {
          Future.successful(
            new Image(new java.io.ByteArrayInputStream(response.body()))
          )
        } else {
          Future.failed(
            throw new RuntimeException(
              s"Failed to fetch icon '$iconName': HTTP ${response.statusCode()}"
            )
          )
        }
      }

}
