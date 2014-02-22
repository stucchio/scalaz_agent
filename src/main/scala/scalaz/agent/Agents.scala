package scalaz.agent

import language.higherKinds

trait Agents {
  trait Agent[T, A <: Agent[T,A]] {
    def receive(d: T): A
  }

  trait EffectfulAgent[T, A <: Agent[T,A], F[_]] extends Agent[T,A] {
    def discard: A
    def effect: F[A]
  }
}
