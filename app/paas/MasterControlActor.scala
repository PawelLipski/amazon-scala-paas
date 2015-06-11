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
  
  def receive = {
    
    case Launch(slaves, params) =>
      val agentNumber = params.map(m => m._2).sum
      val perSlaveMin = agentNumber / slaves.length
      val leftover = agentNumber % slaves.length
      
      for(slave <- slaves zipWithIndex) {
        val thread = new Thread(new Runnable {
		  def run() {
		    Process(s"ssh -i aws-master-key.pem $slave._1 killall java") ! ;
		    Thread.sleep(1000)
		    Process(s"ssh -i aws-master-key.pem $slave._1 cd paas-repo; sbt start -mem 800 < /dev/null") !
		  }
		})
        thread.start
      }
      context.become(active(MutableList(params.toList:_*), perSlaveMin, leftover))
  
    case LaunchResult(refs) => registerLanuched(refs)
      
  }
  
  def registerLanuched(refs: List[ActorRef]) {
    agents.synchronized(agents ++= refs)   
  }
  
  def takeOneOrNone(sum: Int) = if(sum > 0) 1 else 0
  
  def active(params: MutableList[(String, Int)], perSlaveMin: Int, leftover: Int): 
	  Actor.Receive = {
    
    case ReadyToLaunch => 
      
      var taken = 0
      var i = 0
      var toSent: MutableList[(String, Int)] = MutableList()
      val todo = perSlaveMin + takeOneOrNone(leftover)
      while(taken != todo)
      {
        taken += Math.min(todo, params(0)._2)
        for(i <- Range(1, taken))
          toSent.+=((params(0)._1, i))
        if(params(0)._2 >= todo)
        	params(0) = (params(0)._1, params(0)._2 - todo)
        else
        	params(0) = (params(0)._1, 0) 
        i += 1
      }
      
      sender ! LaunchRequest(toSent.toList) 
      
      if(params.last._2 == 0)
        context.become(receive)
      
      if(leftover > 0)
    	  context.become(active(params, perSlaveMin, leftover-1))
      else
    	  context.become(active(params, perSlaveMin, 0))
     
     case LaunchResult(refs) => registerLanuched(refs)
     
  }
}

