package auctionhause

import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import akka.event.Logging
import akka.util.Timeout
import auctionhause.actors.{HouseManager, OpenHouse}
import paas.{FetchActorRef, Agent}
import akka.pattern.ask
import scala.concurrent.duration._

import scala.concurrent.Future
import scala.util.Try

/**
 * Created by bj on 21.10.14.
 */
case class AuctionHouse() extends Agent{

  val system = ActorSystem("AuctionHouse")

  val log = Logging(system, AuctionHouse.getClass.getName)

  implicit val timeout = Timeout(10 seconds)

  import system.dispatcher

  main(null)

  def main(args: Array[String]) {
    log info "*********************AuctionHouse: Auction House has been initialized!"

    //val houseManager = system.actorOf(Props(new HouseManager(system)), "manager")

    val master: ActorSelection = context.actorSelection("akka.tcp://MasterSystem@10.0.0.240:2552/user/master")
    log.info("master selection: " + master)
    val masterRef: Future[ActorRef] = (master ? FetchActorRef("auctionhause.actors.HouseManager1")).mapTo[ActorRef]
    masterRef.onSuccess{
      case ref: ActorRef => {
        log.info("got houseManager ref: " + ref.path)
        ref ! OpenHouse(system)
      }
      case _ => log.info("something is wrong")
    }
    masterRef.onComplete{
      case ref: Try[ActorRef] => {
        log.info("onCom got houseManager ref: " + ref.get.path)
        ref.get ! OpenHouse(system)
      }
      case _ => log.info("on Com something is wrong")
    }
    masterRef.onFailure{
      case throwable : Throwable => log.info("onFailure: " + throwable)
    }
    log.info("after ask, maybe timeout")
  }
}
