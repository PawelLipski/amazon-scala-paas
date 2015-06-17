package auctionhause

import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import akka.event.Logging
import auctionhause.actors.{HouseManager, OpenHouse}
import paas.{FetchActorRef, Agent}
import akka.pattern.ask

import scala.concurrent.Future

/**
 * Created by bj on 21.10.14.
 */
case class AuctionHouse() extends Agent{

  val system = ActorSystem("AuctionHouse")

  val log = Logging(system, AuctionHouse.getClass.getName)

  main(null)

  def main(args: Array[String]) {
    log info "*********************AuctionHouse: Auction House has been initialized!"

    //val houseManager = system.actorOf(Props(new HouseManager(system)), "manager")

    val master: ActorSelection = context.actorSelection("/user/master")
    val houseManager = master ? FetchActorRef("auctionhause.AuctionHouse1")
    houseManager.mapTo[ActorRef].map{ manager =>
      println("got houseManager ref: " + manager.path)
      manager ! OpenHouse(system)
    }
  }
}
