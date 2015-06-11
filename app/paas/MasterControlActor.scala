package paas

import akka.actor.Props
import akka.actor.Actor
import sys.process._
import play.Logger
import scala.language.postfixOps
import scala.collection.mutable.MutableList
import akka.actor.ActorRef

class MasterControlActor extends Actor {
  
  var agents: MutableList[ActorRef] = MutableList()
  var slaveAgents: MutableList[ActorRef] = MutableList()
  
  def receive = {
    
    case Launch(slaves, params) =>
      Logger.info("Launch")
      val agentNumber = params.map(m => m._2).sum
      val perSlaveMin = agentNumber / slaves.length
      val leftover = agentNumber % slaves.length
      
      context.become(active(MutableList(params.toList:_*), perSlaveMin, leftover))
  
    case LaunchResult(refs) => registerLaunched(refs)
      
  }
  
  def registerLaunched(refs: List[ActorRef]) {
    Logger.info("Launch Sucess! " + refs.toString)
    agents.synchronized(agents ++= refs)   
  }
  
  def takeOneOrNone(sum: Int) = if(sum > 0) 1 else 0
  
  def active(params: MutableList[(String, Int)], perSlaveMin: Int, leftover: Int): 
	  Actor.Receive = {
    
    case ReadyToLaunch => 
      Logger.info("ReadyToLaunch")
      
      var isNew = false
      val org = slaveAgents.length
      if(!slaveAgents.exists(ag => ag == sender)) {
          slaveAgents += sender
          isNew = true
      }
      
      if(org == slaveAgents.length)
      	context.become(receive)
      else if (isNew) {
	      var taken = 0
	      var toSent: MutableList[(String, Int)] = MutableList()
	      val todo = perSlaveMin + takeOneOrNone(leftover)
	      
	      Logger.debug("Params: "+params)
	      Logger.debug("TODO: "+todo)
	      var i = 0
	      while(taken != todo)
	      {
	        Logger.debug("i: "+i)
	        taken += Math.min(todo-taken, params(i)._2)
	        Logger.debug("params(i)._2: "+params(i)._2)
	        Logger.debug("taken: "+Math.min(todo-taken, params(i)._2))
	        for(j <- Range(0, Math.min(todo, params(i)._2)))
	          toSent.+=((params(i)._1, j+1))
	        if(params(i)._2 >= todo)
	        	params(i) = (params(i)._1, params(i)._2 - todo)
	        else
	        	params(i) = (params(i)._1, 0) 
	        i += 1
	      }
	      
	      sender ! LaunchRequest(toSent.toList) 
	      
	      if(leftover > 0)
	    	  context.become(active(params, perSlaveMin, leftover-1))
	      else
	    	  context.become(active(params, perSlaveMin, 0))
      }
     case LaunchResult(refs) => registerLaunched(refs)
     
  }
}

