package sample.remote.paas

import akka.actor.Props
import akka.actor.Actor
import sys.process._

class MasterActor extends Actor {
  def receive = {
    case TellMeSomethingMyMaster() =>
      println("*** Sender " + sender().path + " is asking me to tell him something interesting:)!\n")
      val adage = "fortune".!!
      sender() ! Adage(adage)
  }
}

