package paas

import scala.concurrent.duration._
import scala.util.Random
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import scala.collection.mutable.MutableList
import play.Logger

object RunnerApplication {
  
  var system: Option[ActorSystem] = None
  
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
    if(system.isDefined) {
      system.get.shutdown
      system = None
    }
  }
  
  def startRemoteMasterSystem(): Unit = {
    val system = ActorSystem("MasterSystem",
      ConfigFactory.load("master"))
    this.system = Some(system)
    
    system.actorOf(Props[MasterControlActor], "master")

    Logger.info("Started MasterSystem - waiting for messages")
  }

  def startRemoteSlaveSystem(masterIP: String): Unit = {
    val system =
      ActorSystem("SlaveSystem", ConfigFactory.load("slave"))
    this.system = Some(system)
      
    val remoteMasterPath =
      "akka.tcp://MasterSystem@" + masterIP + ":2552/user/master"
    val actor = system.actorOf(Props(classOf[SlaveControlActor], remoteMasterPath), "slave")

    Logger.info("Started SlaveSystem")
    import system.dispatcher
    system.scheduler.schedule(1.second, 5.second) {
      Logger.info("###Master, tell me something interesting please!\n")
      actor ! TellMeSomethingMyMaster
    }
  }
  
  def issueActionToMaster(slaves: List[String], values: Map[String, Int]) {
    val master = this.system.find(system => system.name == "MasterSystem")  
    if(master.isDefined) {
      val system = master.get
      val ref = system.actorSelection("/user/master")
      ref ! Launch(slaves, values)
    }
  }
}
