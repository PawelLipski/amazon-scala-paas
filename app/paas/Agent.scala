package paas

import akka.actor.ActorRef
import akka.actor.Actor

trait Agent extends Actor {
  
	final def receive = {
		case Run(num) => run(num)
		case ShowState => sender ! state
		case any => getMessage(any)
	}

    def run(number: Int) = {} // abstrakcyjna
    def state: String = "" // abstrakcyjna
      
    def getMessage(msg: Any) = {
      sender ! msg
    }
      
    final override def toString = state
}