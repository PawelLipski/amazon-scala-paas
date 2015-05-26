package auctionhause.actors.auction

import akka.actor.ActorRef

/**
 * Created by bj on 26.05.15.
 */
object StatesAndData {

  sealed trait AuctionState

  sealed trait AuctionData

  case object Created extends AuctionState

  case object Activated extends AuctionState

  case object Ignored extends AuctionState

  case object Sold extends AuctionState

  case object NotInitialized extends AuctionState

  case object NotBiddedYet extends AuctionData

  case class Bidded(buyers: List[ActorRef], topBuyer: ActorRef, topPrice: Long) extends AuctionData

  case object NotCreatedYet extends AuctionData

  case object NotInitializedData extends AuctionData

}
