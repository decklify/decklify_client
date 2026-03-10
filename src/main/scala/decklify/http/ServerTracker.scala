package decklify.http

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

object ServerTracker extends ServiceListener {
  private val serverUrl = new AtomicReference[Option[String]](None)
  private val serverFound = CountDownLatch(1)

  override def serviceAdded(event: ServiceEvent): Unit = ()

  override def serviceResolved(event: ServiceEvent): Unit =
    val info = event.getInfo
    val ip = info.getInetAddresses.head.getHostAddress
    val port = info.getPort
    serverUrl.set(Some(s"http://$ip:$port"))
    println(s"Server online: ${serverUrl.get.toString}")
    serverFound.countDown

  override def serviceRemoved(event: ServiceEvent): Unit =
    println("Server went offline")
    serverUrl.set(None)

  def getUrl: Option[String] = serverUrl.get

  def waitForServer(error: () => Unit): Unit =
    if !serverFound.await(10, TimeUnit.SECONDS) then error: Unit
}
