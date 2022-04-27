package composablearchitecture

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class EffectDebounceTests {
    @Test
    fun debounce() = runTest(UnconfinedTestDispatcher()) {

        val values = mutableListOf<Int>()

        suspend fun runDebouncedEffect(value: Int) {
            launch {
                values.addAll(
                    Effect(flow = flowOf(value))
                        .debounce("cancelToken", 1.seconds)
                        .sink()
                )
            }
        }

        runDebouncedEffect(value = 1)

        // Nothing emits right away.
        assertEquals(listOf<Int>(), values)

        // Waiting half the time also emits nothing
        advanceTimeBy(500)
        assertEquals(listOf<Int>(), values)

        // Run another debounced effect.
        runDebouncedEffect(value = 2)

        // Waiting half the time emits nothing because the first debounced effect has been canceled.
        advanceTimeBy(500)
        assertEquals(listOf<Int>(), values)

        // Run another debounced effect.
        runDebouncedEffect(value = 3)

        // Waiting half the time emits nothing because the second debounced effect has been canceled.
        advanceTimeBy(500)
        assertEquals(listOf<Int>(), values)

        // Waiting the rest of the time emits the final effect value.
        advanceTimeBy(501)
        assertEquals(listOf(3), values)

        // Running out the scheduler
        advanceUntilIdle()
        assertEquals(listOf(3), values)
    }

    @Test
    fun debounceIsLazy() = runTest(UnconfinedTestDispatcher()) {

        val values = mutableListOf<Int>()

        var effectRuns = 0

        fun runDebouncedEffect(value: Int) {
            launch {
                values.addAll(
                    Effect(
                        flow = {
                            effectRuns += 1
                            value
                        }.asFlow()
                    )
                        .debounce("cancelToken", 1.seconds)
                        .sink()
                )
            }
        }

        runDebouncedEffect(value = 1)

        assertEquals(listOf<Int>(), values)
        assertEquals(0, effectRuns)

        advanceTimeBy(500)

        assertEquals(listOf<Int>(), values)
        assertEquals(0, effectRuns)

        advanceTimeBy(501)

        assertEquals(listOf<Int>(1), values)
        assertEquals(1, effectRuns)
    }
}
