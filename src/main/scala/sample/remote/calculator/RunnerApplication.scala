package sample.remote.calculator

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
    val remotePath =
      "akka.tcp://MasterSystem@127.0.0.1:2552/user/master"
    val actor = system.actorOf(Props(classOf[SlaveActor], remotePath), "slave")

    println("Started SlaveSystem")
    import system.dispatcher
    system.scheduler.schedule(1.second, 1.second) {
      actor ! Add(Random.nextInt(100), Random.nextInt(100))
    }
  }
}
