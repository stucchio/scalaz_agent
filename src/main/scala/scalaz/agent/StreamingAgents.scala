package scalaz.agent

import scalaz.stream._
import language.higherKinds

trait StreamingAgents {
  def runAgent[F[_], T, A <: Agent[T,A]](process: Process[F,T], initialState: =>A): Process[F,A] =
    process.scan(initialState)(
      (a, t) => a match {
        case (ea:EffectfulAgent[T,A,_]) => ea.discard.receive(t) // State which was previously used for an effect should be discarded
        case aa => aa.receive(t)
      }
    )

  def runAgentForEffect[F[_],F2[x] >: F[x], T, A <: EffectfulAgent[T,A,F2]](process: Process[F,T], initialState: A): Process[F2,A] =
    process.scan(initialState)( (a, t) => a.discard.receive(t) ) // State which was previously used for an effect should be discarded
      .evalMap(_.effect)
}
