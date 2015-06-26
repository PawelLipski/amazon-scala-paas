package paas

trait AgentGroup {

	def addAgent(agent: Agent)
  	def removeAgent(agent: Agent)
    def createSubGroup(name: String): AgentGroup
    def listSubgroups: List[AgentGroup]
    def removeSubgroup(group: AgentGroup)
  
}