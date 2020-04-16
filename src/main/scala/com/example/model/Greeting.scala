package com.example.model

import akka.actor.typed.ActorRef
import eu.timepit.refined.types.string.NonEmptyString

object Greeting {

  /**
   * Trait to represent commands
   * @tparam Res  Response type that shall be returned for the command
   */
  sealed trait Command[Res] {
    def sender:ActorRef[Res]
  }

  /**
   * Trait to represent event
   * @tparam Req  Request type for which the event is created
   */
  sealed trait Event[Req] {
    def sender:ActorRef[Req]
  }

  /**
   * Command to greet supplied subject
   *
   * @param subject Subject to greet
   * @param sender  Actor that issued the command
   */
  final case class Greet(subject: NonEmptyString, sender: ActorRef[Greeted]) extends Command[Greeted]

  /**
   * Confirmation event to indicate the Greet command was handled successfully
   *
   * @param subject Subject that was greeted!
   * @param sender  Actor that handled the command
   */
  final case class Greeted(subject: NonEmptyString, sender: ActorRef[Greet]) extends Event[Greet]

}
