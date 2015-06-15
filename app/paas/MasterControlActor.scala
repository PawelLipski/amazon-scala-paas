package paas

import akka.actor.Props
import akka.actor.Actor
import sys.process._
import play.Logger
import scala.language.postfixOps
import scala.collection.mutable.MutableList
import akka.actor.ActorRef
import scala.collection.mutable.HashMap

class MasterControlActor extends Actor {
  
  var launchedAgentsMap: HashMap[String, ActorRef] = HashMap()
  var launchedAgentsCounters: HashMap[String, Int] = HashMap()
  
  var currentAgents: MutableList[ActorRef] = MutableList()
  
  def receive = {
    
    case Launch(slaves, params) =>
      Logger.info("Launch")
      val agentNumber = params.map(m => m._2).sum
      val perSlaveMin = agentNumber / slaves.length
      val leftover = agentNumber % slaves.length
      
      val list = MutableList(params.toList:_*)
      
      context.become(active(slaves.length, list,
          list.map(_ => 0), perSlaveMin, leftover))
  
    case LaunchResult(refs) => registerLaunched(refs)
      
  }

  def synchronize[T0](x: T0): T0 = 
    launchedAgentsMap.synchronized(launchedAgentsCounters.synchronized(x))
  
  def fetchActorRef(actorName: String) = 
    synchronize(launchedAgentsMap get(actorName))
  
  def registerLaunched(refs: List[(String, String, ActorRef)]) {
    Logger.info("Launch Success! " + refs.toString)
    synchronize(
		for(ref <- refs) {
		  launchedAgentsMap put(ref._1, ref._3)
		  val prev = launchedAgentsCounters.get(ref._2)
		  if(prev.isDefined)
			  launchedAgentsCounters.update(ref._2, prev.get+1)
		  else
			  launchedAgentsCounters.update(ref._2, 1)
		}
    )   
  }
  
  def takeOneOrNone(sum: Int) = if(sum > 0) 1 else 0
  
  def active(slaveCount: Int, params: MutableList[(String, Int)], sent: MutableList[Int],
      perSlaveMin: Int, leftover: Int): 
	  Actor.Receive = {
    
    case ReadyToLaunch => 
      Logger.info("got ReadyToLaunch")
      
      var isNew = false
      if(!currentAgents.exists(ag => ag == sender)) {
          currentAgents += sender
          isNew = true
      }
      
      if(((currentAgents.length == slaveCount) && !isNew)
          || params.forall(f => f._2 == 0)) {
        currentAgents.clear
        Logger.debug("current agents: "+currentAgents)
        context.become(receive)
      } else if (isNew) {
	      var taken = 0
	      var toSent: MutableList[(String, Int)] = MutableList()
	      val todo = perSlaveMin + takeOneOrNone(leftover)
	      
	      Logger.debug("current agents: "+currentAgents)
	      Logger.debug("Params: "+params)
	      Logger.debug("TODO: "+todo)
	      
	      var i = 0
	      while(taken != todo)
	      {
	        Logger.debug("i: "+i)
	        val choice = Math.min(todo-taken, params(i)._2)
	        taken += choice
	        Logger.debug("params(i)._2: "+params(i)._2)
	        Logger.debug("taken: "+choice)
	        
	        if(params(i)._2 >= todo)
	        	params(i) = (params(i)._1, params(i)._2 - todo)
	        else
	        	params(i) = (params(i)._1, 0) 
	        
	        synchronize(
		        for(j <- Range(1, choice+1))
		          toSent += ((params(i)._1, j+sent(i)+
		              launchedAgentsCounters(params(i)._1)))
	        )
	        sent(i) += choice
	          
	        i += 1
	      }
	      
	      Logger.debug(toSent.toList.toString)
	      sender ! LaunchRequest(toSent.toList) 
	      
	      if(currentAgents.length == slaveCount) {
	    	  currentAgents.clear
	    	  Logger.debug("current agents: "+currentAgents)
		  	  context.become(receive)
	      }
	      
	      if(leftover > 0)
	    	  context.become(active(slaveCount, params, sent, perSlaveMin, leftover-1))
	      else
	    	  context.become(active(slaveCount, params, sent, perSlaveMin, 0))
      }
     case LaunchResult(refs) => registerLaunched(refs)
     
  }
}

