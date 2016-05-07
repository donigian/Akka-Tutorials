import RelentlessWorkabee.{StopException, ContinueException, RestartException}
import akka.actor.SupervisorStrategy.{Escalate, Stop, Resume, Restart}
import akka.actor._

/**
  * Created by arm on 5/6/16.
  */
class RelentlessWorkabee  extends Actor {

  override def preStart() = {
    println("Workabee preStart hook")
  }

  override def preRestart(reason: Throwable, msg: Option[Any]) = {
    println("Workabee preRestart hook")
    super.preRestart(reason, msg)
  }

  override def postRestart(reason: Throwable) = {
    println("Workabee postRestart hook")
    super.postRestart(reason)
  }

  override def postStop() = {
    println("Workabee postStop")
  }

  def receive = {
    case "Continue" =>
      throw ContinueException
    case "Stop" =>
      throw StopException
    case "Restart" =>
      throw RestartException
    case _ =>
      throw new Exception
  }
}

object RelentlessWorkabee {
  case object ContinueException extends Exception
  case object StopException extends Exception
  case object RestartException extends Exception
}

class RelentlessManager extends Actor {
  import RelentlessWorkabee._
  import scala.concurrent.duration._

  var childRef: ActorRef = _
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 1 second) {
      case ContinueException => Resume
      case RestartException => Restart
      case StopException => Stop
      case _: Exception => Escalate
    }

  override def preStart() = {
    childRef = context.actorOf(Props[RelentlessWorkabee], "RelentlessManager")
    Thread.sleep(50)
  }

  def receive = {
    case msg =>
      println(s"Relentless Manager ${msg}")
      childRef ! msg
      Thread.sleep(50)
  }
}

object Supervisor extends App {

  val system = ActorSystem("Supervisor")

  val manager = system.actorOf(Props[RelentlessManager], "RelentlessManager")

//  manager ! "Continue"
//  Thread.sleep(50)
//  println()

  manager ! "Restart"
  Thread.sleep(500)
  println()

  system.terminate()
}