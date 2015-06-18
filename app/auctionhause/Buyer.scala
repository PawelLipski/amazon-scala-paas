package auctionhause

import akka.actor.Status.Success
import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import akka.event.Logging
import akka.util.Timeout
import paas.{FetchActorRef, Agent, ShowState}
import akka.pattern.ask
import scala.concurrent.duration._

import scala.concurrent.Future
import scala.concurrent.Await
import scala.util.Try

case object MakeBid

case class Buyer() extends Agent {

  val system = ActorSystem("AuctionHouse")
  val log = Logging(system, "Buyer")

  import scala.language.postfixOps
  implicit val timeout = Timeout(30 seconds)

  import system.dispatcher

  var biddingCurrent = 300

  val master: ActorSelection = context.actorSelection("akka.tcp://MasterSystem@10.0.0.240:2552/user/master")
  val auction1 = Await.result(master ? FetchActorRef("auctionhause.Auction1"), 15 seconds).asInstanceOf[ActorRef]

  self ! MakeBid

  /*.onComplete {
    case ref: Try[ActorRef] =>
      log.info("Got Auction1 ref: " + ref.get)
      ref.get ! Bid(600)
    case other => 
	  log.info("Something was wrong: " + other.toString)
  }*/

  override def receive = {
	case ShowState =>
	  log info ("Received show state from " + sender)
	  sender ! ("Bidding at $" + 600)
	case MakeBid =>
	  auction1 ! Bid(biddingCurrent)
	  biddingCurrent += 100
	  context.system.scheduler.scheduleOnce(5 seconds, self, MakeBid)
  }
}

