import TalkingMachinesPodcastPlayer.{StartPodcast, StopPodcast}
import TalkingMachinesPodcastController.{Play, Stop}
import akka.actor.{ActorSystem, Props, Actor}

/**
  * Created by arm on 5/6/16.
  */
object TalkingMachinesPodcastController {
  // Sealed traits must be extended in same file where they are defined
  // Alternative to enums
  sealed trait ControllerMessage
  case object Play extends ControllerMessage
  case object Stop extends ControllerMessage
  def props = Props[TalkingMachinesPodcastController]
}

class TalkingMachinesPodcastController extends Actor {
  def receive = {
    case Play =>
      println("Podcast started...")
    case Stop =>
      println("Podcast stopped...")
  }
}

object TalkingMachinesPodcastPlayer {
  sealed trait PlayMessage
  case object StopPodcast extends PlayMessage
  case object StartPodcast extends PlayMessage
}

class TalkingMachinesPodcastPlayer extends Actor {
  def receive = {
    case StopPodcast =>
      println("Stopping Podcast")
    case StartPodcast =>
      val controller = context.actorOf(TalkingMachinesPodcastController.props, "controller")
      controller ! Play
    case _ =>
      println("Message not recognized")
  }
}

object Creation extends App {
  val system = ActorSystem("creation")

  val player = system.actorOf(Props[TalkingMachinesPodcastPlayer], "player")

  player ! StartPodcast
}