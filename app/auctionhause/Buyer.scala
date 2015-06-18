package auctionhause

import akka.actor.Status.Success
import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props, Actor}
import akka.event.Logging
import akka.util.Timeout
import paas.{FetchActorRef, Agent, ShowState}
import akka.pattern.ask
import akka.event.LoggingReceive
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import scala.concurrent.Future
import scala.concurrent.Await
import scala.util.Try

case class Buyer() extends Agent {

  //val system = ActorSystem("AuctionHouse")
  //val log = Logging(system, "Buyer")

  import scala.language.postfixOps
  import ExecutionContext.Implicits.global
  implicit val timeout = Timeout(30 seconds)

  var biddingCurrent = 200

  val master: ActorSelection = context.actorSelection("akka.tcp://MasterSystem@10.0.0.240:2552/user/master")
  var auction1: ActorRef = _

  case object MakeBid
  case object Init

  context.system.scheduler.scheduleOnce(5 seconds, self, Init)
  
  override def receive = {
	case ShowState =>
	  //log info ("Received show state from " + sender)
	  sender ! "initializing"

	case Init =>
	  auction1 = Await.result(master ? FetchActorRef("auctionhause.Auction1"), 2 seconds).asInstanceOf[ActorRef]
	  context become inited
	  self ! MakeBid
  }

  def inited = LoggingReceive {
	case ShowState =>
	  //log info ("Received show state from " + sender)
	  sender ! ("bidding for one ton of bananas at $" + biddingCurrent)
	case MakeBid =>
	  biddingCurrent += 100
	  auction1 ! Bid(biddingCurrent)
	  context.system.scheduler.scheduleOnce(5 seconds, self, MakeBid)
  }
}

