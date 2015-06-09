package paas

import akka.actor.ActorRef
import akka.actor.Actor

trait Agent extends Actor {
  
	def getActorReference(name: String) = {} // ActorRef = {
        // tutaj będzie implementacja, coś na zasadzie:
        // masterControlActor ! FetchActorRef(name) 
//    -> wait for response => ActorRef do szukanego aktora
        // kwestia do ogarnięcia, jak masterControlActor zostanie przekazany do Agenta
        // konstruktor czy coś, nie istotne aż tak
	// }
    def run(id: Int) = {} // abstrakcyjna - to w niej będziemy korzystać z getActorReference
    def state: String = "" // abstrakcyjna

    override def toString = state
}