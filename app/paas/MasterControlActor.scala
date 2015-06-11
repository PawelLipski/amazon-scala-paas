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
      Logger.info("Launch")
      val agentNumber = params.map(m => m._2).sum
      val perSlaveMin = agentNumber / slaves.length
      val leftover = agentNumber % slaves.length
      
      for(slave <- slaves zipWithIndex) {
        val thread = new Thread(new Runnable {
		  def run() {
		    Process("ssh -i aws-master-key.pem "+slave._1+" killall java") ! ;
		    Thread.sleep(1000)
		    Process("ssh -i aws-master-key.pem "+slave._1+" cd paas-repo; sbt start -mem 800 < /dev/null") !
		  }
		})
        thread.start
      }
      Thread.sleep(10000)
      context.become(active(MutableList(params.toList:_*), perSlaveMin, leftover))
  
    case LaunchResult(refs) => registerLanuched(refs)
      
  }
  
  def registerLanuched(refs: List[ActorRef]) {
    Logger.info("Launch Sucess! " + refs.toString)
    agents.synchronized(agents ++= refs)   
  }
  
  def takeOneOrNone(sum: Int) = if(sum > 0) 1 else 0
  
  def active(params: MutableList[(String, Int)], perSlaveMin: Int, leftover: Int): 
	  Actor.Receive = {
    
    case ReadyToLaunch => 
      Logger.info("ReadyToLaunch")
      var taken = 0
      var toSent: MutableList[(String, Int)] = MutableList()
      val todo = perSlaveMin + takeOneOrNone(leftover)
      
      var i = 0
      while(taken != todo)
      {
        taken += Math.min(todo, params(i)._2)
        for(j <- Range(1, taken))
          toSent.+=((params(i)._1, j))
        if(params(i)._2 >= todo)
        	params(i) = (params(i)._1, params(i)._2 - todo)
        else
        	params(i) = (params(i)._1, 0) 
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

