import akka.actor._

/**
  * Created by arm on 5/7/16.
  */
class WatchDog(applicationProcess: ActorRef) extends Actor {

  override def preStart() = {
    context.watch(applicationProcess)
  }

  override def postStop() = {
    println("WatchDog postStop")
  }

  def receive = {
    case Terminated =>
      context.stop(self)
  }
}

class ApplicationProcess extends Actor {

  def receive = {
    case msg =>
      println(s"Application process received the following ${msg}")
      context.stop(self)
  }
}

object WatchDogMonitor extends App {

  val system = ActorSystem("WatchDogMonitor")

  val applicationProcess = system.actorOf(Props[ApplicationProcess], "applicationProcess")

  val watchDog = system.actorOf(Props(classOf[WatchDog], applicationProcess), "watchDog")

  applicationProcess ! "Boom"

  system.terminate()

}
