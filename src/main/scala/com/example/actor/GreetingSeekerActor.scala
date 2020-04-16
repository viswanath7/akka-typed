package com.example.actor

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric._

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.example.model.Greeting._

object GreetingSeekerActor {

  def apply(timesToGreet: Int Refined Positive): Behavior[Greeted] = {

    def behaviour(greetingCount: Int = 1): Behavior[Greeted] =
      Behaviors.receive { (context, greeted) =>
        context.log.info("#{} '{}' actor greeted {}", greetingCount, greeted.sender.path.name, greeted.subject)
        if (greetingCount + 1 > timesToGreet) {
          Behaviors.stopped
        } else {
          //context.log.info("Sending a 'Greet' message with subject '{}' to {} ...", greeted.subject, greeted.sender.path.name)
          greeted.sender ! Greet(greeted.subject, context.self)
          behaviour(greetingCount + 1)
        }
      }


    behaviour()
  }

}
