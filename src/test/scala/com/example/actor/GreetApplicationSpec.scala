package com.example.actor

import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.Effect.{Spawned, SpawnedAnonymous}
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.typed.Behavior
import com.example._
import com.example.model.Greeting._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.event.Level

class GreetApplicationSpec extends AnyWordSpecLike with Matchers {


  "Greeter actor" must {
    "spawn GreeterActor & GreetingSeekerActor and send a transformed message to spawned child GreeterActor" in {
      val behaviourTestKit = BehaviorTestKit(GreetApplication())
      val greeter = behaviourTestKit.expectEffectType[Spawned[Greet]]
      behaviourTestKit.logEntries() shouldBe
        Seq(CapturedLogEvent(Level.INFO, "Creating a child 'GreeterActor' with name 'Greeter' ..."))


      behaviourTestKit.run( SayHello("John") )
      val greeterMailbox = behaviourTestKit.childInbox(greeter.ref)
      greeterMailbox.receiveMessage() should have (
        Symbol("subject") ("John")
      )

      behaviourTestKit.expectEffectPF{
        case Spawned(behaviour,name, _) =>
          behaviour shouldBe a [Behavior[Greeted]]
          name shouldBe "John"
      }
    }
  }

}
