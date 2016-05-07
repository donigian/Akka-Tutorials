import MailingList.AddListener
import MembershipChecker.{LegitListener, FraudulentListener, CheckListener}
import PodcastRecorder.NewListener
import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import akka.util.Timeout
import scala.language.postfixOps
import akka.pattern.ask

/**
  * Created by arm on 5/6/16.
  */

case class Listener(listinerName: String, email: String)

object PodcastRecorder {
  sealed trait RecorderMessage
  case class NewListener(listener: Listener) extends RecorderMessage

  def props(membershipChecker: ActorRef, mailingList: ActorRef) =
    Props(new MembershipRecorder(membershipChecker, mailingList))
}

object MembershipChecker {
  sealed trait MembershipCheckerMessage
  case class CheckListener(listener: Listener) extends MembershipCheckerMessage

  sealed trait MembershipCheckerResponse
  case class FraudulentListener(listener: Listener) extends MembershipCheckerMessage
  case class LegitListener(listener: Listener) extends MembershipCheckerMessage
}

object MailingList {
  sealed trait MailingListMessage
  case class AddListener(listener: Listener) extends MailingListMessage
}

class MailingList extends Actor {
  var listeners = List.empty[Listener]

  def receive = {
    case AddListener(listener) =>
      println(s"Mailing List: $listener added")
      listeners = listener :: listeners
  }
}

class MembershipChecker extends Actor {
  val fraudList = List(
    Listener("AlphaGo", "alphago@haha.com")
  )

  def receive = {
    case CheckListener(listener) if fraudList.contains(listener) =>
      println(s"MembershipChecker: $listener is fraudulent")
      sender() ! FraudulentListener(listener)
    case CheckListener(listener) =>
      println(s"MembershipChecker: $listener is legit")
      sender() ! LegitListener(listener)

  }
}

class MembershipRecorder(checker: ActorRef, mailingList: ActorRef) extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  implicit val timeout = Timeout(5 seconds)

  def receive = {
    case NewListener(listener) =>
      MembershipChecker ? CheckListener(listener) map {
        case LegitListener(listener) =>
          MailingList ! AddListener(listener)
        case FraudulentListener(listener) =>
          println(s"Membership Recorder: $listener is fraudulent")
      }
  }
}

object TalkingMachinesPodcastSimulator extends App {

  val system = ActorSystem("TalkingMachinesPodcastSimulator")

  val checker = system.actorOf(Props[MembershipChecker], "membershipChecker")

  val storage = system.actorOf(Props[MailingList], "mailingList")

  val recorder = system.actorOf(MembershipRecorder.props(checker,storage), "MembershipRecorder")

  recorder ! MembershipRecorder.NewListener(Listener("Daneil", "watson@ibmm.com"))

  Thread.sleep(100)

  system.terminate()

}