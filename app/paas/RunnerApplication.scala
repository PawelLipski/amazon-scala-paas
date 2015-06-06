package paas

import scala.concurrent.duration._
import scala.util.Random
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import scala.collection.mutable.MutableList
import play.Logger

object RunnerApplication {
  
  var systemObjects: MutableList[ActorSystem] = MutableList()
  
  def main(args: Array[String]): Unit = {
    if (args.isEmpty || args.head == "Master")
      startRemoteMasterSystem()
    if (args.isEmpty || args.head == "Slave"){
      if (args.length > 1){
        startRemoteSlaveSystem(args(1))
      } else {
        startRemoteSlaveSystem("127.0.0.1")
      }
    }
  }

  def stop(): Unit = {
    systemObjects.synchronized({
        systemObjects.map(sys => sys.shutdown)
        systemObjects.clear
    })
  }
  
  def startRemoteMasterSystem(): Unit = {
    val system = ActorSystem("MasterSystem",
      ConfigFactory.load("master"))
    systemObjects.synchronized(systemObjects += system)
    
    system.actorOf(Props[MasterControlActor], "master")

    Logger.info("Started MasterSystem - waiting for messages")
  }

  def startRemoteSlaveSystem(masterIP: String): Unit = {
    val system =
      ActorSystem("SlaveSystem", ConfigFactory.load("slave"))
    systemObjects.synchronized(systemObjects += system)
      
    val remoteMasterPath =
      "akka.tcp://MasterSystem@" + masterIP + ":2552/user/master"
    val actor = system.actorOf(Props(classOf[SlaveControlActor], remoteMasterPath), "slave")

    Logger.info("Started SlaveSystem")
    import system.dispatcher
    system.scheduler.schedule(1.second, 5.second) {
      Logger.info("###Master, tell me something interesting please!\n")
      actor ! TellMeSomethingMyMaster()
    }
  }
}
