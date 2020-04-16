package com.example.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.example.model.Greeting._

object GreeterActor {

  /**
   * Behaviour of the Actor.
   *
   * @return  Behaviour of the Actor
   */
  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Hello {}!", message.subject)
    // Send a reply back to the sender of the message with reference to one self as sender
    message.sender ! Greeted(message.subject, context.self)
    // Next behaviour will be the same as the current one.
    // Message processing does not result in change in behaviour and update of state.
    Behaviors.same
  }
}
