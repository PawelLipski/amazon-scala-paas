package auctionhause

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import auctionhause.actors.{HouseManager, OpenHouse}

/**
 * Created by bj on 21.10.14.
 */
object AuctionHouse {

  val system = ActorSystem("AuctionHouse")

  val log = Logging(system, AuctionHouse.getClass.getName)

  def main(args: Array[String]) {
    log info "AuctionHouse: Auction House has been initialized!"

    val houseManager = system.actorOf(Props(new HouseManager(system)), "manager")

    houseManager ! OpenHouse
  }
}
