package sample.remote.calculator

import akka.actor.Props
import akka.actor.Actor

class MasterActor extends Actor {
  def receive = {
    case Add(n1, n2) =>
      println("Calculating %d + %d".format(n1, n2))
      sender() ! AddResult(n1, n2, n1 + n2)
  }
}

