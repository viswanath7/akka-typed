package com.example.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.example._
import com.example.model.Greeting._
import com.typesafe.scalalogging.LazyLogging
import eu.timepit.refined.auto._

import scala.language.postfixOps

final case class SayHello(name: String)

/**
 * Actor that spawns
 * 1. a single GreeterActor as a child and
 * 2. a GreetingSeekerActor for every message of type 'SayHello' that it receives and
 * starts the interaction between a single GreeterActor and possibly multiple instances of GreetingSeekerActor.
 */
object GreetApplication extends App with LazyLogging {

  // Defer the creation of behaviour until the actor is started
  def apply(): Behavior[SayHello] =  Behaviors.setup { context =>

      context.log.info("Creating a child 'GreeterActor' with name 'Greeter' ...")
      val greeterActor = context.spawn(GreeterActor(), "Greeter")

      Behaviors.receiveMessage { sayHelloMessage =>

        context.log.info("Creating a child 'GreetingSeekerActor' with name {}", sayHelloMessage.name)
        val greetingSeekerActor = context.spawn(GreetingSeekerActor(timesToGreet = 3), sayHelloMessage.name)

        greeterActor ! Greet(sayHelloMessage.name.toNonEmptyString, greetingSeekerActor)
        Behaviors.same
      }
    }
  logger.info("Creating the actor system ...")
  val actorSystem: ActorSystem[SayHello] = ActorSystem(GreetApplication(), "Greeting-actor-system")


  import monix.eval._
  import monix.execution.Scheduler.Implicits.global

  // Create a list of tasks where each task sends a SayHello message
  val independentTasks = List("World", "Akka")
    .map(SayHello)
    .map(msg => Task {actorSystem ! msg} )
  // Execute the tasks in parallel and upon completion, terminate the actor system
  Task.gather(independentTasks)
    .doOnFinish(possibleError =>
      possibleError
        .toLeft("")
        .fold(error => {
          logger.error("Task to greet completed with error!", error)
          Task raiseError error
        }, _ => Task.unit)
        .map(_=> {
          logger.info("Terminating actor system ...")
          actorSystem.terminate()
        }))
    .runAsyncAndForget
}
