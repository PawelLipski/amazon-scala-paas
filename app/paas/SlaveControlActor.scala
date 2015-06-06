package paas

import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorIdentity
import akka.actor.ActorRef
import akka.actor.Identify
import akka.actor.ReceiveTimeout
import akka.actor.Terminated
import play.Logger

class SlaveControlActor(path: String) extends Actor {

  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit = {
    context.actorSelection(path) ! Identify(path)
    import context.dispatcher
    context.system.scheduler.scheduleOnce(3.seconds, self, ReceiveTimeout)
  }

  def receive = identifying

  def identifying: Actor.Receive = {
    case ActorIdentity(`path`, Some(actor)) =>
      context.watch(actor)
      context.become(active(actor))
    case ActorIdentity(`path`, None) => Logger.info(s"Remote actor not available: $path")
    case ReceiveTimeout              => sendIdentifyRequest()
    case _                           => Logger.info("Not ready yet")
  }

  def active(actor: ActorRef): Actor.Receive = {
    case op: ReqMessage => actor ! op
    case result: ResMessage => result match {
      case Adage(text) =>
        Logger.info("%%% Master said: " + text + "\n")
    }
    case Terminated(`actor`) =>
      Logger.info("Master terminated")
      sendIdentifyRequest()
      context.become(identifying)
    case ReceiveTimeout =>
    // ignore

  }
}
