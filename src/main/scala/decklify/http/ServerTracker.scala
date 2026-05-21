package decklify.http

import java.util.concurrent.atomic.AtomicReference
import javax.jmdns.JmmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object ServerTracker extends ServiceListener {
  private val serverUrl = new AtomicReference[Option[String]](None)
  private var jmdns: Option[JmmDNS] = None
  private val SERVICE_TYPE = "_decklify._tcp.local."

  def init(j: JmmDNS): Unit =
    jmdns = Some(j)

  override def serviceAdded(event: ServiceEvent): Unit =
    event.getDNS.requestServiceInfo(event.getType, event.getName, 1000)

  override def serviceResolved(event: ServiceEvent): Unit =
    val info = event.getInfo
    val ip = info.getInetAddresses.head.getHostAddress
    val port = info.getPort
    serverUrl.set(Some(s"http://$ip:$port"))
    println(s"Server online: ${serverUrl.get.toString}")

  override def serviceRemoved(event: ServiceEvent): Unit =
    println("Server went offline, re-querying...")
    serverUrl.set(None)

    Future {
      Thread.sleep(2000)
      jmdns.foreach(_.listBySubtype(SERVICE_TYPE))
    }(using ExecutionContext.global): Unit

  def getUrl: Option[String] = serverUrl.get

  def waitForServer(error: () => Unit): Unit =
    val deadline = System.currentTimeMillis() + 10000
    while serverUrl.get.isEmpty && System.currentTimeMillis() < deadline do
      Thread.sleep(500)
    if serverUrl.get.isEmpty then error()
}
