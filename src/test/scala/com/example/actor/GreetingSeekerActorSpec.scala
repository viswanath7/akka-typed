package com.example.actor

import akka.actor.testkit.typed.scaladsl.LoggingTestKit
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import com.example.model.Greeting.{Greet, Greeted}
import eu.timepit.refined.api.Refined
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive

class GreetingSeekerActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike with Matchers {

  private[this] val numberOfTimesToGreet:Int Refined Positive = 5
  private[this] val greetingSeeker: ActorRef[Greeted] = testKit.spawn(GreetingSeekerActor(numberOfTimesToGreet), "greetingSeekerActor")
  private[this] val testProbe = testKit.createTestProbe[Greet]()

  private[this] val testProbeMockedBehaviour = Behaviors.receive[Greet] { (context, greetMessage) =>
    context.log.info("Probe received Greet('{}') from {}", greetMessage.subject, greetMessage.sender.path)
    greetMessage.sender ! Greeted(greetMessage.subject, context.self)
    Behaviors.same
  }

  private[this] val mockedGreeter = testKit.spawn(Behaviors.monitor(testProbe.ref, testProbeMockedBehaviour))

  "GreetingSeekerActor actor" must {
    val testMessage = "John"

    "greet and send Greeted response" in {
      import com.example._
      LoggingTestKit
        .info("actor greeted John")
        .withOccurrences(numberOfTimesToGreet)
        .expect {
          mockedGreeter ! Greet(testMessage.toNonEmptyString, greetingSeeker.ref)
          testProbe.expectMessageType[Greet]
        }
    }

    "not accept invalid message types because GreetingSeekerActor is a typed actor" in {
      "greetingSeeker ! 12345" shouldNot typeCheck
    }

  }

}
