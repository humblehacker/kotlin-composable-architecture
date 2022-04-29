package composablearchitecture

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration

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

    suspend fun sink(): List<Output> {
        val outputs = mutableListOf<Output>()
        flow.toList(outputs)
        return outputs
    }
}

fun <State, Output> State.withNoEffect(): Result<State, Output> =
    Result(this, Effect.none())

fun <State, Output> State.withEffect(
    block: suspend FlowCollector<Output>.() -> Unit
): Result<State, Output> =
    Result(this, Effect(flow(block)))
