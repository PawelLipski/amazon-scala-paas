package paas

import akka.actor.Actor

trait Agent extends Actor {
  
	final def receive = {
		case Run(args) => init(args)
		case ShowState => sender ! state
		case any => getMessage(any)
	}
      
    def getMessage(msg: Any): PartialFunction[Any,Unit]
    def init(args: Any*)
    def state: String
    def id: String = this.getClass().getCanonicalName()
    
    def getAvailableResponses: List[Class[Any]] = List()
    def saveToBlackboard(key: String, value: Any) = {}
    def takeFromBlackboard(key: String): Any = {}
    def joinGroup(group: AgentGroup) = {}
    def leaveGroup(group: AgentGroup) = {}
    def listGroupsContaining: List[AgentGroup] = List()
    
    final override def toString = state
}