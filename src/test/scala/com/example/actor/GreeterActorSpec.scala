package com.example.actor

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.LoggingTestKit
import com.example.model.Greeting.{Greet, Greeted}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GreeterActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike with Matchers {

  private[this] val greeter = testKit.spawn(GreeterActor(), "greeterActor")
  private[this] val testProbe = testKit.createTestProbe[Greeted]()

  "Greeter actor" must {
    val messageContent = "hello"

    "greet and send Greeted response" in {
      import com.example._
      LoggingTestKit.info(s"Hello $messageContent!").expect {
        greeter ! Greet(messageContent.toNonEmptyString, testProbe.ref)
      }
      testProbe.expectMessage(Greeted(messageContent.toNonEmptyString, greeter.ref))
    }

    "not accept invalid message types because Greeter is a typed actor" in {
      "greeter ! messageContent" shouldNot typeCheck
    }

  }

}
