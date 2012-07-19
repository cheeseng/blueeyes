package blueeyes.persistence.cache

import org.scalatest._
import org.scalatest.time._
import org.scalatest.concurrent._
import java.util.concurrent.TimeUnit.{MILLISECONDS}


import scala.util.Random
import scalaz._

import blueeyes.bkka.AkkaDefaults
import akka.dispatch.Future
import akka.dispatch.Future._
import akka.actor.Actor
import akka.actor.Props
import akka.pattern.ask
import akka.util.Duration
import akka.util.Timeout

import java.util.concurrent.TimeUnit

class StageSpec extends WordSpec with MustMatchers with Eventually with AkkaDefaults {
  private val random    = new Random()
  implicit val timeout = akka.util.Timeout(100000)
  override implicit val patienceConfig =
    PatienceConfig(timeout = scaled(Span(100, Seconds)), interval = scaled(Span(100, Millis)))
  implicit val StringSemigroup = new Semigroup[String] {
    def append(s1: String, s2: => String) = s1 + s2
  }

  "Stage" should {
    "evict when entry is expired" in {
      @volatile var evicted = false

      val stage = newStage(None, Some(20), (key: String, value: String) => evicted = evicted || (key == "foo" && value == "bar"))

      stage.put("foo", "bar")
      stage.put("bar", "baz")

      eventually { evicted must be (true) }

      // because the eventually matcher reevaluates the LHS, use two lines so that stop only is called once
      val stopFuture = stage.stop(timeout)
      eventually { stopFuture.isCompleted must be (true) }
    }

    "evict when stage is over capacity" in{
      @volatile var evicted = false

      val stage = newStage(None, None, (key: String, value: String) => evicted = key == "foo2" && value == "bar2", 1)
      stage.put("foo2", "bar2")
      stage.put("bar2", "baz2")

      eventually { evicted must be (true) }

      // because the eventually matcher reevaluates the LHS, use two lines so that stop only is called once
      val stopFuture = stage.stop(timeout)
      eventually { stopFuture.isCompleted must be (true) }
    }

    "evict when stage is flushed" in{
      @volatile var evicted = false

      val stage = newStage(Some(1), None, (key: String, value: String) => evicted = key == "foo3" && value == "bar3")
      stage.put("foo3", "bar3")
      stage.flushAll(timeout)

      eventually { evicted must be (true) }

      // because the eventually matcher reevaluates the LHS, use two lines so that stop only is called once
      val stopFuture = stage.stop(timeout)
      eventually { stopFuture.isCompleted must be (true) }
    }

    "evict automatically" in{
      @volatile var evicted = false

      val stage = newStage(Some(10), None, (key: String, value: String) => evicted = key == "foo4" && value == "bar4")
      stage.put("foo4", "bar4")

      eventually { evicted must be (true) }

      // because the eventually matcher reevaluates the LHS, use two lines so that stop only is called once
      val stopFuture = stage.stop(timeout)
      eventually { stopFuture.isCompleted must be (true) }
    }

    "evict automatically more then one time" in{
      @volatile var evictCount = 0

      val stage = newStage(Some(10), None, (key: String, value: String) => if (key == "foo" && value == "bar") evictCount += 1 )

      stage.put("foo", "bar")

      eventually { evictCount must be (1) }

      stage.put("foo", "bar")

      eventually { evictCount must equal (2) }

      // because the eventually matcher reevaluates the LHS, use two lines so that stop only is called once
      val stopFuture = stage.stop(timeout)
      eventually { stopFuture.isCompleted must be (true) }
    }

    // this test runs for a really long time
    "evict all messages when multiple threads send messages" in {
      val messagesCount = 100
      val threadsCount  = 20

      @volatile var collected = 0
      val stage     = newStage(Some(10), None, (key: String, value: String) => collected = collected + (value.length / key.length))
      val actors    = List.fill(threadsCount) {
        defaultActorSystem.actorOf(Props(new MessageActor("1", "1", messagesCount, stage)))
      }

      val futures   = Future.sequence(actors.map(actor => (actor ? "Send").mapTo[Unit]))
      eventually { futures.value.isDefined must be (true) }

      val flushFuture = stage.flushAll(timeout)
      eventually { flushFuture.value.isDefined must be (true) }

      collected must equal (messagesCount * threadsCount)

      actors.foreach(_ ! _root_.akka.actor.PoisonPill)
      
      // because the eventually matcher reevaluates the LHS, use two lines so that stop only is called once
      val stopFuture = stage.stop(timeout)
      eventually { stopFuture.isCompleted must be (true) }
    }

    // this test runs for a really long time
    "evict all messages when multiple threads send messages with different keys" in {
      val messagesCount           = 50
      val threadsPerMessagesType  = 10
      val threadsCount            = 20

      val messages: List[List[String]]  = List.range(0, threadsCount) map {i => for (j <- 0 until threadsPerMessagesType) yield (List(i.toString)) } flatten

      val collected = new scala.collection.mutable.HashMap[String, Int]()
      val stage     = newStage(Some(10), None, {(key: String, value: String) =>
        val count = collected.get(key) match {
          case Some(x) => x
          case None => 0
        }
        collected.put(key, count + (value.length / key.length))
      })
      val actors    = messages map {msgs =>
        defaultActorSystem.actorOf(Props(new MessageActor(msgs(0), msgs(0), messagesCount, stage)))
      }

      val futures = Future.sequence(actors.map(actor => (actor ? "Send").mapTo[Unit]))
      eventually { futures.value.isDefined must be (true) }

      val flushFuture = stage.flushAll(timeout)
      eventually { flushFuture.value.isDefined must be (true) }

      collected must equal(Map[String, Int](messages.distinct.map(v => (v(0), threadsPerMessagesType * messagesCount)): _*))

      actors.foreach(_ ! _root_.akka.actor.PoisonPill)
      
      // because the eventually matcher reevaluates the LHS, use two lines so that stop only is called once
      val stopFuture = stage.stop(timeout)
      eventually { stopFuture.isCompleted must be (true) }
    }
  }

  private def newStage[T](timeToIdle: Option[Long], timeToLive: Option[Long], evict: (String, String) => Unit, capacity: Int = 10) = {
    Stage[String, String](ExpirationPolicy(timeToIdle, timeToLive, MILLISECONDS), capacity, evict)
  }

  class MessageActor(key: String, message: String, size: Int, stage: Stage[String, String]) extends Actor{
    def receive = {
      case "Send" => {
        for (j <- 0 until size){
          Thread.sleep(random.nextInt(100))
          stage.put(key, message)
        }

        sender ! ()
      }
      case _ =>
    }
  }
}
