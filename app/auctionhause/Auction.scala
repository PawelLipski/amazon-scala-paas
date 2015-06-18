package auctionhause

import akka.actor.Status.Success
import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import akka.event.Logging
import akka.util.Timeout
import paas.{FetchActorRef, Agent, ShowState}
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import scala.concurrent.Future
import scala.util.Try

case class Bid(price: Int)

case class Auction() extends Agent {

  //val system = ActorSystem("AuctionHouse")
  //val log = Logging(system, "Auction")

  import scala.language.postfixOps
  import ExecutionContext.Implicits.global
  implicit val timeout = Timeout(30 seconds)

  val product = "one ton of bananas"
  var maxBid = 100

  override def receive = {
	case ShowState =>
	  //log info ("Received show state from " + sender)
	  sender ! ("selling " + product + " for $" + maxBid)
	case Bid(price) =>
	  if (price > maxBid)
	    maxBid = price
  }
}

