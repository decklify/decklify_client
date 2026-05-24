package decklify.http

import java.util.concurrent.atomic.AtomicReference
import javax.jmdns.JmmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object ServerTracker extends ServiceListener {
  private val serverUrl = new AtomicReference[Option[String]](None)
  private val lastSet = new AtomicReference[String]("")
  private var jmdns: Option[JmmDNS] = None
  private val SERVICE_TYPE = "_decklify._tcp.local."

  @volatile private var polling = false

  def init(j: JmmDNS): Unit =
    jmdns = Some(j)
    startPolling()

  private def startPolling(): Unit =
    if polling then return

    polling = true
    Future {
      while polling do
        if serverUrl.get.isEmpty then
          println("Polling for server...")
          jmdns.foreach { j =>
            j.list(SERVICE_TYPE, 3000).foreach { info =>
              val ip = info.getInetAddresses
                .filterNot(a => a.isLoopbackAddress || a.isLinkLocalAddress)
                .headOption
                .map(_.getHostAddress)
                .getOrElse(info.getInetAddresses.head.getHostAddress)
              val port = info.getPort
              val url = s"http://$ip:$port"
              if lastSet.getAndSet(url) != url then
                serverUrl.set(Some(url))
                println(s"Server rediscovered via poll: $ip:$port")
            }
          }
          Thread.sleep(5000)
        else Thread.sleep(5000)
    }(using ExecutionContext.global): Unit

  def stopPolling(): Unit = polling = false

  override def serviceAdded(event: ServiceEvent): Unit =
    event.getDNS.requestServiceInfo(event.getType, event.getName, 1000)

  override def serviceResolved(event: ServiceEvent): Unit =
    val info = event.getInfo
    val ip = info.getInetAddresses.head.getHostAddress
    val port = info.getPort
    val url = s"http://$ip:$port"

    if lastSet.getAndSet(url) != url then
      serverUrl.set(Some(url))
      println(s"Server online: $ip:$port")

  override def serviceRemoved(event: ServiceEvent): Unit =
    if serverUrl.getAndSet(None).isDefined then
      lastSet.set("")
      println("Server went offline, polling will rediscover...")

  def getUrl: Option[String] = serverUrl.get

  def waitForServer(error: () => Unit): Unit =
    val deadline = System.currentTimeMillis() + 10000
    while serverUrl.get.isEmpty && System.currentTimeMillis() < deadline do
      Thread.sleep(500)
    if serverUrl.get.isEmpty then error()
}
