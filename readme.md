# Scalaz Agents

This is a tiny little library built on top of [scalaz-streaming](https://github.com/scalaz/scalaz-stream), which provides a more typesafe variation of actor like functionality. It is loosely inspired by Clojure agents, python greenlets, as well as Akka.

There are several primary design goals:

Separating state changes from side effects. This makes testing and reasoning easier, and makes effects more modular.

Avoiding the overhead of Akka actors. Akka actors always run in separate threads, and each new message requires a trip through the threadpool. That is not always necessary. Scalaz streaming allows you to abstract over those details.

Type safety. Akka has type `Any => Unit`. Typed actors provide a partial solution to this.

# Agents

An `Agent` is a class which represents the state of a computation - you can think of it as being an immutable and typesafe version of an akka `Actor`. All the work is done in the `receive` method. For example:

    import scalaz.agent._

    case class CounterAgent(count: Long = 0) extends Agent[Int,CounterAgent] {
      def receive(d: Int) = CounterAgent(count+d)
    }

Instead of mutating the internal state of the `Agent`, we simply return a new copy.

An `Agent` can be run on a `scalaz.streaming.Process` object using the `runAgent` method:

    import scalaz._
    import scalaz.stream._

    val inputStream = Process.emitSeq(Seq(1,1,2,0,1))

    runAgent(inputStream, CounterAgent())

The result will be a stream of the form:

    Seq(CounterAgent(0), CounterAgent(1), CounterAgent(2), CounterAgent(4), CounterAgent(4), CounterAgent(5))

Each element represents the state of the actor at a given time. Of course, different types of streams will not necessarily store the entire computation history.

# EffectfulAgent

An `EffectfulAgent` is an agent that can perform side effects. It must implement two additional methods, `discard` and `effect`.

Another example to illustrate. This is an example of an `EffectfulAgent` which logs data into a `Writer[List[Long],_]` monad. You can choose the monad that an `EffectfulAgent` runs in.

    case class CounterLoggingAgent(count: Long = 0, inputsSeen: Long = 0, toWrite: Option[Long] = None) extends EffectfulAgent[Int,CounterLoggingAgent,IO] {
      def receive(d: Int) = {
        if (((inputsSeen+1) % 10) == 0) {
          CounterLoggingAgent(count+d, inputsSeen+1, toWrite = Some(count+d))
        } else {
          CounterLoggingAgent(count+d, inputsSeen+1)
        }
      }
      def discard = this.copy(toWrite=None)
      def effect = toWrite.map( putStrLn ).getOrElse( ioUnit ).map(_ => this.discard)
    }

(For those unfamiliar with Scalaz's `IO` monad, `putStrLn` is a monadic print while `ioUnit` is a do-nothing within the `IO` monad function.)

The `effect` method should be the *only* method which produces any side effects. The `discard` method should return a new agent object which will perform no side effects.

The following will produce *no side effects*:

    runAgent(inputStream, CounterLoggingAgent())

To call the side effects, you must use the `runAgentForEffect` method:

    runAgentForEffect(inputStream, CounterLoggingAgent())

This will return a `Process[IO,CounterLoggingAgent]`. This can then be run via the normal scalaz-stream methods to produce side effects.

## EffectfulAgent laws

 The `EffectfulAgent` should satisfy the following laws:

    a.discard.effect == mzero

(Assuming of course you are in a monad with zero.)

    a.discard.discard == a.discard

Pretty straightforward. Lastly, if we ignore effects:

    a.effect == a.discard.point[F]
