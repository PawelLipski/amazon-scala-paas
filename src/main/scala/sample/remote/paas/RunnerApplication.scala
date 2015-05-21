package sample.remote.paas

import scala.concurrent.duration._
import scala.util.Random
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props

object RunnerApplication {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty || args.head == "Master")
      startRemoteMasterSystem()
    if (args.isEmpty || args.head == "Slave")
      startRemoteSlaveSystem()
  }

  def startRemoteMasterSystem(): Unit = {
    val system = ActorSystem("MasterSystem",
      ConfigFactory.load("master"))
    system.actorOf(Props[MasterActor], "master")

    println("Started MasterSystem - waiting for messages")
  }

  def startRemoteSlaveSystem(): Unit = {
    val system =
      ActorSystem("SlaveSystem", ConfigFactory.load("slave"))
    val remoteMasterPath =
      "akka.tcp://MasterSystem@127.0.0.1:2552/user/master"
    val actor = system.actorOf(Props(classOf[SlaveActor], remoteMasterPath), "slave")

    println("Started SlaveSystem")
    import system.dispatcher
    system.scheduler.schedule(1.second, 5.second) {
      actor ! TellMeSomethingMyMaster()
    }
  }
}
