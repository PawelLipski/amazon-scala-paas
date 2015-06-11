package paas

import akka.actor.Props
import akka.actor.Actor
import sys.process._
import play.Logger
import scala.language.postfixOps

class MasterControlActor extends Actor {
  def receive = {
    
    case Launch(slaves, params) =>
      val agentNumber = params.map(m => m._2).sum
      val perSlaveMin = agentNumber / slaves.length
      val leftover = agentNumber % slaves.length
      
      for(slave <- slaves) {
        val thread = new Thread(new Runnable {
		  def run() {
		    Process(s"ssh -i aws-master-key.pem $slave killall java") ! ;
		    Process(s"ssh -i aws-master-key.pem $slave cd paas-repo; sbt start -mem 800 < /dev/null") !
		  }
		})
        thread.start
      }
      
    
    case TellMeSomethingMyMaster =>
      Logger.info("ping!")
      Logger.info("*** Sender " + sender.path + " is asking me to tell him something interesting:)!\n")
      val adage = Process("fortune") !! ;
      sender ! Adage(adage)
  }
}

