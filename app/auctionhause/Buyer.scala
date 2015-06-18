package auctionhause

import akka.actor.Status.Success
import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import akka.event.Logging
import akka.util.Timeout
import paas.{FetchActorRef, Agent, ShowState}
import akka.pattern.ask
import scala.concurrent.duration._

import scala.concurrent.Future
import scala.util.Try

case class Buyer() extends Agent {

  val system = ActorSystem("AuctionHouse")
  val log = Logging(system, "Buyer")

  import scala.language.postfixOps
  implicit val timeout = Timeout(30 seconds)

  import system.dispatcher

  val master: ActorSelection = context.actorSelection("akka.tcp://MasterSystem@10.0.0.240:2552/user/master")
  (master ? FetchActorRef("auctionhause.Auction1")).mapTo[ActorRef].onComplete {
    case ref: Try[ActorRef] =>
      log.info("Got Auction1 ref: " + ref.get)
      ref.get ! Bid(600)
    case other => 
	  log.info("Something was wrong: " + other.toString)
  }

  override def receive = {
	case ShowState =>
	  sender ! ("Bidding at $" + 600)
  }
}

