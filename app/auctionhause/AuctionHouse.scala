package auctionhause

import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import akka.event.Logging
import akka.util.Timeout
import auctionhause.actors.{HouseManager, OpenHouse}
import paas.{FetchActorRef, Agent}
import akka.pattern.ask
import scala.concurrent.duration._

import scala.concurrent.Future

/**
 * Created by bj on 21.10.14.
 */
case class AuctionHouse() extends Agent{

  val system = ActorSystem("AuctionHouse")

  val log = Logging(system, AuctionHouse.getClass.getName)

  implicit val timeout = Timeout(30 seconds)

  import system.dispatcher

  main(null)

  def main(args: Array[String]) {
    log info "*********************AuctionHouse: Auction House has been initialized!"

    //val houseManager = system.actorOf(Props(new HouseManager(system)), "manager")

    val master: ActorSelection = context.actorSelection("akka.tcp://MasterSystem@10.0.0.240:2552/user/master")
    log.info("master selection: " + master)
    (master ? FetchActorRef("auctionhause.actors.HouseManager1")).mapTo[ActorRef].onComplete{
      case ref: ActorRef =>
        log.info("got houseManager ref: " + ref.path)
        ref ! OpenHouse(system)
      case other => log.info("Something was wrong: " + other.toString)
    }
    log.info("after ask, maybe timeout")
  }
}
