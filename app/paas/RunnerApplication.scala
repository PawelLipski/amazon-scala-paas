package paas

import java.net.NetworkInterface

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import paas.GetRunningAgents
import play.Logger

import scala.collection.JavaConversions._
import scala.concurrent.Future

object RunnerApplication {

  var system: Option[ActorSystem] = None

  def main(args: Array[String]): Unit = {
    if (args.isEmpty || args.head == "Master")
      if (args.length > 1) {
        startRemoteMasterSystem(args(1))
      } else {
        startRemoteMasterSystem("127.0.0.1")
      }
    if (args.isEmpty || args.head == "Slave") {
      if (args.length > 1) {
        startRemoteSlaveSystem(args(1))
      } else {
        startRemoteSlaveSystem("127.0.0.1")
      }
    }
  }

  def stop(): Unit = {
    if (system.isDefined) {
      system.get.shutdown()
      system = None
    }
  }

  def startRemoteMasterSystem(masterIP: String): Unit = {
    val ips =
      for {
        interface <- NetworkInterface.getNetworkInterfaces
        address <- interface.getInetAddresses
      } yield address.getHostAddress

    val system = ActorSystem("MasterSystem",
      ConfigFactory.load("master").
        withValue("remote.netty.tcp.hostname",
          ConfigValueFactory.fromAnyRef(masterIP)))

    this.system = Some(system)

    system.actorOf(Props[MasterControlActor], "master")
    Logger.info("Started MasterSystem - waiting for messages")
  }

  def startRemoteSlaveSystem(masterIP: String): Unit = {

    val ips =
      for {
        interface <- NetworkInterface.getNetworkInterfaces
        address <- interface.getInetAddresses
      } yield address.getHostAddress

    val ip = ips.find(ip => ip.startsWith("10.0.0.1")).get

    val system =
      ActorSystem("SlaveSystem", ConfigFactory.load("slave").
        withValue("remote.netty.tcp.hostname",
          ConfigValueFactory.fromAnyRef(ip)))
    this.system = Some(system)
    Logger.info("Started SlaveSystem")

    system.actorOf(Props(classOf[SlaveControlActor],
      "akka.tcp://MasterSystem@" + masterIP + ":2552/user/master"), "slave")

    /*val actor = system.actorOf(Props[MasterControlActor].withDeploy
        (Deploy(scope = RemoteScope(AddressFromURIString(remoteMasterPath))))
    , ip)
    
    val selection =
      system.actorSelection("akka.tcp://MasterSystem@" + masterIP + ":2552/user/master")
    
    import system.dispatcher
    system.scheduler.schedule(1.second, 10.second) {
      Logger.info("@"+ip+" Remote Deployed Master tell me something interesting please!\n")
      actor ! TellMeSomethingMyMaster
      
      selection ! TellMeSomethingMyMaster
    }*/

  }

  def issueActionToMaster(slaves: List[String], values: Map[String, Int]) {
    val master: Option[ActorSystem] = getMasterSystem
    if (master.isDefined) {
      val system = master.get
      val ref = system.actorSelection("/user/master")
      ref ! Launch(slaves, values)
    }
  }

  def getMasterSystem: Option[ActorSystem] = this.system.find(system => system.name == "MasterSystem")

  def getMasterActor: Option[ActorSelection] = {
    getMasterSystem.map(_.actorSelection("/user/master"))
  }

  import akka.pattern.ask

  def getListOfRunning(implicit tm: Timeout): Future[RunningAgents] =
    getMasterActor.map(_ ? GetRunningAgents)
      .map(_.mapTo[RunningAgents])
      .getOrElse(Future.successful(RunningAgents(Map.empty)))

  def performStop(name: String) =
    getMasterActor.foreach(_ ! KillAgent(name))
}
