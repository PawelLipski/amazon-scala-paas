package paas

import akka.actor.Props
import akka.actor.Actor
import sys.process._
import play.Logger

class MasterControlActor extends Actor {
  def receive = {
    case TellMeSomethingMyMaster() =>
      Logger.info("*** Sender " + sender.path + " is asking me to tell him something interesting:)!\n")
      val adage = "fortune".!!
      sender ! Adage(adage)
  }
}

