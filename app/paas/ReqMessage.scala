package paas

import akka.actor.ActorRef

trait ReqMessage

trait ResMessage

case object ReadyToLaunch extends ResMessage

case class Run(num: Int) extends ReqMessage

case object Stop extends ReqMessage

case object ShowState extends ReqMessage

case class Launch(slaves: List[String], values: Map[String, Int]) extends ReqMessage

case class LaunchRequest(agentSpec: List[(String, Int)]) extends ReqMessage

case class LaunchResult(agents: List[(String, String, ActorRef)]) extends ReqMessage

case object GetRunningAgents

case class RunningAgents(agents: Map[String, ActorRef])

case class KillAgent(agentName: String)