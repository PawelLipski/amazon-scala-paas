package paas

import scala.concurrent.duration._
import scala.util.Random
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import scala.collection.mutable.MutableList
import play.Logger
import com.typesafe.config.ConfigValueFactory
import java.net.NetworkInterface
import scala.collection.JavaConversions._
import akka.actor.Deploy
import akka.remote.RemoteScope
import akka.actor.AddressFromURIString

object RunnerApplication {
  
  var system: Option[ActorSystem] = None
  
  def main(args: Array[String]): Unit = {
    if (args.isEmpty || args.head == "Master")
      if (args.length > 1){
        startRemoteMasterSystem(args(1))
      } else {
        startRemoteMasterSystem("127.0.0.1")
      }
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
  
  def startRemoteMasterSystem(masterIP: String): Unit = {
    val ips = 
      for(interface <- NetworkInterface.getNetworkInterfaces();
    	address <- interface.getInetAddresses()) 
      yield address.getHostAddress()   
    
    val system = ActorSystem("MasterSystem",
      ConfigFactory.load("master").
      	withValue("remote.netty.tcp.hostname", 
          ConfigValueFactory.fromAnyRef(masterIP)))  
      
    this.system = Some(system)
    
    system.actorOf(Props[MasterControlActor], "master")

    Logger.info("Started MasterSystem - waiting for messages")
    
    val remoteMasterPath =
      "akka.tcp://SlaveSystem@10.0.1.188:2553/user/slavetest"
    //val actor = system.actorOf(Props(classOf[SlaveControlActor], remoteMasterPath), "slave1")
 
    import system.dispatcher
    /*system.scheduler.schedule(1.second, 5.second) {
      Logger.info("###Slave, tell me something interesting please!\n")
      actor ! TellMeSomethingMyMaster
    }  */
      
  }

  def startRemoteSlaveSystem(masterIP: String): Unit = {
   
    val ips = 
      for(interface <- NetworkInterface.getNetworkInterfaces();
    	address <- interface.getInetAddresses()) 
      yield address.getHostAddress()   
    
    val ip = ips.find(ip => ip.startsWith("10.0.1")).get
      
    val system =
      ActorSystem("SlaveSystem", ConfigFactory.load("slave").
          withValue("remote.netty.tcp.hostname", 
              ConfigValueFactory.fromAnyRef(ip)))
    this.system = Some(system)
    
    val remoteMasterPath =
      "akka.tcp://MasterSystem@" + masterIP + ":2552/user/master"+ip
    
    val actor = system.actorOf(Props[MasterControlActor].withDeploy
        (Deploy(scope = RemoteScope(AddressFromURIString(remoteMasterPath))))
    , ips.find(ip => ip.startsWith("10.0.1")).get)

    Logger.info("Started SlaveSystem")
    
    import system.dispatcher
    system.scheduler.schedule(1.second, 5.second) {
      Logger.info("## Master "+ips.find(ip => ip.startsWith("10.0.1")).get+
          " , tell me something interesting please!\n")
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
