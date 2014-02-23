package com.chrisstucchio.spire_examples

import scalaz._
import Scalaz._
import scalaz.agent._
import scalaz.stream.{Writer => StreamWriter, _}

import org.scalacheck._
import org.scalacheck.Arbitrary.arbitrary
import Prop._

object AgentSpecification extends Properties("AgentSpecification") {

  case class CounterAgent(count: Long = 0) extends Agent[Int,CounterAgent] {
    def receive(d: Int) = CounterAgent(count+d)
  }

  property("CounterAgent history works properly") = forAll( (input: List[Boolean]) => {
    val data = Process.emitSeq(input.map(_ => 1))
    val agentHistory: Process[Nothing,CounterAgent] = runAgent(data,CounterAgent())
    val expectedHistory = (List(CounterAgent(0)) ++ input.zipWithIndex.map( i => CounterAgent(i._2+1) ))
    (agentHistory.toSeq == expectedHistory)
  })

  type LongWriter[X] = Writer[List[Long],X]
  implicit val LongWriterCatchable = new Catchable[LongWriter] {
    def attempt[A](f: LongWriter[A]) = f.map(a => \/-(a))
    def fail[A](err: Throwable) = ???
  }

  case class CounterLoggingAgent(count: Long = 0, inputsSeen: Long = 0, toWrite: Option[Long] = None) extends EffectfulAgent[Int,CounterLoggingAgent,LongWriter] {
    def receive(d: Int) = {
      if (((inputsSeen+1) % 10) == 0) {
        CounterLoggingAgent(count+d, inputsSeen+1, toWrite = Some(count+d))
      } else {
        CounterLoggingAgent(count+d, inputsSeen+1)
      }
    }
    def discard = this.copy(toWrite=None)
    def effect = toWrite.map( x => List(x).tell ).getOrElse( ().point[LongWriter] ).map(_ => this.discard)
  }

  property("CounterLoggingAgent works properly") = forAll( (input: List[Boolean]) => {
    val data = Process.emitSeq(input.map(_ => 1))
    val agentOutput: LongWriter[Unit] = runAgentForEffect[Nothing,LongWriter,Int,CounterLoggingAgent](data,CounterLoggingAgent()).run
    val expectedOutput: List[Long] = input.zipWithIndex.filter(x => (x._2+1) % 10 == 0).map(_._2.toLong+1)
    agentOutput.run._1 == expectedOutput
  })

}
