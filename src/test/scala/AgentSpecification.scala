package com.chrisstucchio.spire_examples

import scalaz._
import Scalaz._
import scalaz.agent._
import scalaz.stream._

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



}
