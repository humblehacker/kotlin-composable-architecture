@file:OptIn(ExperimentalTime::class)

package composablearchitecture

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class Effect<Output>(internal var flow: Flow<Output>) {

    companion object {
        operator fun <Output> invoke(
            block: suspend FlowCollector<Output>.() -> Unit
        ): Effect<Output> = Effect(flow(block))

        fun <Output> none() = Effect<Output>(emptyFlow())
    }

    fun <T> map(transform: (Output) -> T): Effect<T> = Effect(flow.map { transform(it) })

    fun concatenate(vararg effects: Effect<Output>) {
        flow = flowOf(flow, *effects.map { it.flow }.toTypedArray()).flattenConcat()
    }

    fun merge(vararg effects: Effect<Output>) {
        flow = flowOf(flow, *effects.map { it.flow }.toTypedArray()).flattenMerge()
    }

    fun debounce(id: Any, timeout: Duration): Effect<Output> {
        return Effect(flow.onStart { delay(timeout) })
            .cancellable(id, cancelInFlight = true)
    }

    suspend fun sink(collector: FlowCollector<Output>) {
        flow.collect(collector)
    }
}

fun <T> Flow<T>.asEffect(): Effect<T> = Effect(this)

fun <State, Output> State.withNoEffect(): Result<State, Output> =
    Result(this, Effect.none())

fun <State, Output> State.withEffect(
    block: suspend FlowCollector<Output>.() -> Unit
): Result<State, Output> =
    Result(this, Effect(flow(block)))

fun <State, Output> State.withEffect(flow: Flow<Output>): Result<State, Output> =
    Result(this, Effect(flow))

fun <State, Output> State.withEffect(effect: Effect<Output>) =
    Result(this, effect)
