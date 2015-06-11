package paas

import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorIdentity
import akka.actor.ActorRef
import akka.actor.Identify
import akka.actor.ReceiveTimeout
import akka.actor.Terminated
import play.Logger
import akka.actor.Props

class SlaveControlActor(masterPath: String) extends Actor {

  sendReadyToLaunch()

  def sendReadyToLaunch(): Unit = {
    Logger.info("sendReadyToLaunch")
    context.actorSelection(masterPath) ! ReadyToLaunch
    import context.dispatcher
    context.system.scheduler.scheduleOnce(3.seconds, self, ReceiveTimeout)
  }

  def receive = {
    case LaunchRequest(agentSpec) =>
      Logger.info("LaunchRequest")
      Logger.info(agentSpec.toString)
      val agents = agentSpec.map(agent => 
        (context.actorOf(
            Props(Class.forName(agent._1)), agent._1+agent._2), agent._2))       
            
      for(agent <- agents)
        agent._1 ! Run(agent._2)
        
      val refs = agents.map(f => f._1)
      sender ! LaunchResult(refs)
      //context.become(active(refs))
    case ReceiveTimeout              => sendReadyToLaunch()
    case _                           => Logger.info("Unknown message")
  }

  def active(agentSpec: List[ActorRef]): Actor.Receive = {
    
    case Stop =>
      Logger.info("Master terminated")
      
    case _ => // ignore
  }
}
