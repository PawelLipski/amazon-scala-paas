package auctionhause.actors.auction

import akka.actor.{Actor, ActorRef, ActorSystem, FSM}
import auctionhause.actors._
import auctionhause.actors.auction.StatesAndData._
import scala.concurrent.duration._
import paas.Agent

/**
  * Created by bj on 21.10.14.
 */
case class EnglishAuction(bidTime: FiniteDuration, deleteTime: FiniteDuration, system: ActorSystem, auctionSearchName: String) extends Agent with FSM[AuctionState, AuctionData]{

  import system.dispatcher

  val auctionSearchPath: String = "../../" + auctionSearchName

  startWith(NotInitialized, NotInitializedData)

  when(NotInitialized) {
    case Event(Start, _) => {
      log.info("Auction: {} is being started, bidTime: {}, deleteTime: {}", getName, bidTime, deleteTime)
      goto(Created) using NotBiddedYet
    }

  }

  onTransition{
    case NotInitialized -> Created => {
      for((NotInitializedData, NotBiddedYet) <- Some(stateData, nextStateData)) {
        context.actorSelection(auctionSearchPath) ! Register
        system.scheduler.scheduleOnce(bidTime, self, BidTimeExpired)
      }
    }
    case Ignored -> Created => {
      context.actorSelection(auctionSearchPath) ! Register
      system.scheduler.scheduleOnce(bidTime, self, BidTimeExpired)
    }
    case Created -> Ignored => {
      system.scheduler.scheduleOnce(deleteTime, self, DeleteTimeExpired)
      context.actorSelection(auctionSearchPath) ! Unregister
    }
    case Activated -> Sold =>{
      context.actorSelection(auctionSearchPath) ! Unregister
    }
  }

  when(Created){
    case Event(BidTimeExpired, _)=>{
      log.info("Auction: {} reached BidTime: {}", getName, bidTime)
      context.parent ! BeingIgnored
      goto(Ignored)
    }
    case Event(Bid(price), NotBiddedYet) => {
      log.info("Auction: Buyer {} is biding with ${}", sender.path.name, price)
      goto (Activated) using Bidded(List(sender), sender, price)
    }
  }

  when(Activated){
    case Event(Bid(price), Bidded(currentBuyers, topBuyer, topPrice)) => {
      log.info("Auction: Buyer {} is biding with ${}", sender.path.name, price)
      if (price > topPrice){
        (currentBuyers diff List(sender)).foreach(_ ! NewTopBuyer(price, sender))
        stay using Bidded(sender :: (currentBuyers diff List(sender)), sender, price)
      } else {
        sender ! CurrentOfferIsHigher(topPrice)
        stay
      }
    }
    case Event(BidTimeExpired, Bidded(currentBuyers, topBuyer, topPrice)) => {
      log.info("Auction: {} reached BidTime: {}", getName, bidTime)
      system.scheduler.scheduleOnce(deleteTime, self, DeleteTimeExpired)
      topBuyer ! WonTheAuction(topPrice)
      goto(Sold)
    }
  }

  def getName: String = {
    context.parent.path.name + '/' + self.path.name
  }

  when(Ignored){
    case Event(DeleteTimeExpired, _) => {
      log.info("Auction: {} reached DeleteTime: {}", getName, deleteTime)
      context stop self
      stay
    }
    case Event(Relist, _) => {
      log.info("Auction: {} is being relisted now", getName)
      goto(Created) using NotBiddedYet
    }
  }

  when(Sold){
    case Event(DeleteTimeExpired, _) => {
      log.info("Auction: {} reached DeleteTime: {}", getName, deleteTime)
      context stop self
      stay
    }
    case Event(_, _) =>
      stay
  }
  
}


