@file:OptIn(ExperimentalTime::class)

package composablearchitecture

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

private val mutex = Mutex()
private val cancellationJobs: MutableMap<Any, MutableSet<Job>> = mutableMapOf()

fun <Output> Effect<Output>.cancellable(id: Any, cancelInFlight: Boolean = false): Effect<Output> =
    Effect(channelFlow {
        if (cancelInFlight) {
            mutex.withLock {
                cancellationJobs[id]?.forEach { it.cancel() }
                cancellationJobs.remove(id)
            }
        }

        coroutineScope {
            val deferred = async(start = CoroutineStart.LAZY) {
                flow.cancellable().collect { send(it) }
            }

            mutex.withLock {
                @Suppress("RemoveExplicitTypeArguments")
                cancellationJobs.getOrPut(id) { mutableSetOf<Job>() }.add(deferred)
            }

            try {
                deferred.start()
                deferred.await()
            } finally {
                mutex.withLock {
                    val jobs = cancellationJobs[id]
                    jobs?.remove(deferred)
                    if (jobs.isNullOrEmpty()) {
                        cancellationJobs.remove(id)
                    }
                }
            }
        }
    })

fun <Output> Effect.Companion.cancel(id: Any): Effect<Output> = Effect(flow {
    mutex.withLock {
        cancellationJobs[id]?.forEach { it.cancel() }
        cancellationJobs.remove(id)
    }
})

fun <State, Output> State.cancel(id: Any): Result<State, Output> =
    Result(this, Effect.cancel(id))

fun <State, Output> Result<State, Output>.cancellable(
    id: Any,
    cancelInFlight: Boolean = false
): Result<State, Output> =
    Result(state, effect.cancellable(id, cancelInFlight))

fun <State, Output> Result<State, Output>.debounce(
    id: Any,
    timeout: Duration
): Result<State, Output> =
    Result(state, effect.debounce(id, timeout))
